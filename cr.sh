#!/usr/bin/env bash
# Based on work from https://github.com/helm/chart-releaser-action
# As the chart is already packaged, just uploading it

set -o errexit
set -o nounset
set -o pipefail

DEFAULT_CHART_RELEASER_VERSION=v1.4.1

main() {
    
    local version="$DEFAULT_CHART_RELEASER_VERSION"
    local config=
    local chart_tar=
    local owner=
    local repo=
    local charts_repo_url=
    local install_dir=
    local install_only=

    parse_command_line "$@"

    local repo_root
    repo_root=$(git rev-parse --show-toplevel)
    pushd "$repo_root" > /dev/null

    install_chart_releaser

    rm -rf .cr-release-packages
    mkdir -p .cr-release-packages
    cp $chart_tar .cr-release-packages/

    echo "Release dir : `ls -la .cr-release-packages/`"

    rm -rf .cr-index
    mkdir -p .cr-index

    update_index

    popd > /dev/null
}

install_chart_releaser() {
    if [[ ! -d "$RUNNER_TOOL_CACHE" ]]; then
        echo "Cache directory '$RUNNER_TOOL_CACHE' does not exist" >&2
        exit 1
    fi

    if [[ ! -d "$install_dir" ]]; then
        mkdir -p "$install_dir"

        echo "Installing chart-releaser on $install_dir..."
        curl -sSLo cr.tar.gz "https://github.com/helm/chart-releaser/releases/download/$version/chart-releaser_${version#v}_linux_amd64.tar.gz"
        tar -xzf cr.tar.gz -C "$install_dir"
        rm -f cr.tar.gz
    fi

    echo 'Adding cr directory to PATH...'
    export PATH="$install_dir:$PATH"
}

parse_command_line() {
    while :; do
        case "${1:-}" in
            -h|--help)
                show_help
                exit
                ;;
            --config)
                if [[ -n "${2:-}" ]]; then
                    config="$2"
                    shift
                else
                    echo "ERROR: '--config' cannot be empty." >&2
                    show_help
                    exit 1
                fi
                ;;
            -v|--version)
                if [[ -n "${2:-}" ]]; then
                    version="$2"
                    shift
                else
                    echo "ERROR: '-v|--version' cannot be empty." >&2
                    show_help
                    exit 1
                fi
                ;;
            -d|--chart-tar)
                if [[ -n "${2:-}" ]]; then
                    chart_tar="$2"
                    shift
                else
                    echo "ERROR: '-d|--chart-tar' cannot be empty." >&2
                    show_help
                    exit 1
                fi
                ;;
            -u|--charts-repo-url)
                if [[ -n "${2:-}" ]]; then
                    charts_repo_url="$2"
                    shift
                else
                    echo "ERROR: '-u|--charts-repo-url' cannot be empty." >&2
                    show_help
                    exit 1
                fi
                ;;
            -o|--owner)
                if [[ -n "${2:-}" ]]; then
                    owner="$2"
                    shift
                else
                    echo "ERROR: '--owner' cannot be empty." >&2
                    show_help
                    exit 1
                fi
                ;;
            -r|--repo)
                if [[ -n "${2:-}" ]]; then
                    repo="$2"
                    shift
                else
                    echo "ERROR: '--repo' cannot be empty." >&2
                    show_help
                    exit 1
                fi
                ;;
            -n|--install-dir)
                if [[ -n "${2:-}" ]]; then
                    install_dir="$2"
                    shift
                fi
                ;;
            -i|--install-only)
                if [[ -n "${2:-}" ]]; then
                    install_only="$2"
                    shift
                fi
                ;;
            *)
                break
                ;;
        esac

        shift
    done

    if [[ -z "$owner" ]]; then
        echo "ERROR: '-o|--owner' is required." >&2
        show_help
        exit 1
    fi

    if [[ -z "$repo" ]]; then
        echo "ERROR: '-r|--repo' is required." >&2
        show_help
        exit 1
    fi

    if [[ -z "$charts_repo_url" ]]; then
        charts_repo_url="https://$owner.github.io/$repo"
    fi

    if [[ -z "$install_dir" ]]; then
        local arch
        arch=$(uname -m)
        install_dir="$RUNNER_TOOL_CACHE/cr/$version/$arch"
    fi

    if [[ -n "$install_only" ]]; then
        echo "Will install cr tool and not run it..."
        install_chart_releaser
        exit 0
    fi
}

update_index() {
    local args=(-o "$owner" -r "$repo" -p ".cr-release-packages/" --release-name-template "{{ .Version }}" --push --pages-index-path "." --pages-branch "main" -i ".")
    if [[ -n "$config" ]]; then
        args+=(--config "$config")
    fi

    echo 'Updating charts repo index...'
    cr index "${args[@]}"
}

main "$@"