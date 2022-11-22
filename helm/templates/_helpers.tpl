{{/*
Expand the name of the chart.
*/}}
{{- define "jetstream-mini-controller.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "jetstream-mini-controller.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- if contains $name .Release.Name -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{- define "jetstream-mini-controller.container.image" -}}
{{- $imageName :=  .Values.image.name -}}
{{- $imageName = .Values.image.native | ternary (printf "%s%s" $imageName "-native") $imageName -}}
{{- if .Values.global.infra.imageRegistry -}}
image: {{ .Values.global.infra.imageRegistry }}/{{ $imageName }}:{{ .Values.image.version }}
# Always pull images in local dev if registry specified (avoid tricky issues in iterative testing)
imagePullPolicy: {{ .Values.global.imagePullPolicy | default "Always" }}
{{- else -}}
image: {{ $imageName }}:{{ .Values.image.version }}
imagePullPolicy: {{ .Values.global.imagePullPolicy | default "IfNotPresent" }}
{{- end -}}
{{- end -}}

{{/*
Resources limits and requests
*/}}
{{- define "jetstream-mini-controller.resources" -}}
{{- $ephemeralStorage := .Values.image.native | ternary .Values.global.resources.native.requests.ephemeralStorage .Values.global.resources.jvm.requests.ephemeralStorage -}}
{{- $ephemeralStorage = .Values.resources.requests.ephemeralStorage | default $ephemeralStorage -}}
{{- $cpu := .Values.image.native | ternary .Values.global.resources.native.requests.cpu .Values.global.resources.jvm.requests.cpu -}}
{{- $cpu = .Values.resources.requests.cpu | default $cpu -}}
{{- $memory := .Values.image.native | ternary .Values.global.resources.native.requests.memory .Values.global.resources.jvm.requests.memory -}}
{{- $memory = .Values.resources.requests.memory | default $memory -}}
limits:
    ephemeral-storage: {{ $ephemeralStorage }}
    memory: {{ $memory }}
requests:
    ephemeral-storage: {{ $ephemeralStorage }}
    cpu: {{ $cpu }}
    memory: {{ $memory }}
{{- end -}}

{{/*
Custom environment variables
*/}}
{{- define "jetstream-mini-controller.environment.variables" -}}
{{/* Microservice variables have override priority */}}
{{- $environmentVariables := merge .Values.env .Values.global.env -}}
{{- range $name, $value := $environmentVariables }}
- name: "{{ $name }}"
{{- if or (typeIs "string" $value) (typeIs "bool" $value) (typeIs "int" $value) (typeIs "float64" $value) }}
  value: "{{ tpl (toString $value) $ }}"
{{- else }}
{{ toYaml $value | indent 2 }}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "jetstream-mini-controller.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "jetstream-mini-controller.labels" -}}
helm.sh/chart: {{ include "jetstream-mini-controller.chart" . }}
{{ include "jetstream-mini-controller.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "jetstream-mini-controller.selectorLabels" -}}
app.kubernetes.io/name: {{ include "jetstream-mini-controller.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Pod labels
*/}}
{{- define "jetstream-mini-controller.podLabels" -}}
{{- range $name, $value := .Values.podLabels }}
{{ $name }}: {{ tpl $value $ | quote }}
{{- end }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "jetstream-mini-controller.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "jetstream-mini-controller.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}


{{/*
Create the config with streams and key-values
*/}}
{{- define "jetstream-mini-controller.config" -}}
streamConfigs:
{{- if .Values.streams }}
{{- range $key, $value := .Values.streams }}
  - streamName: {{ $value.streamName }}
    subjects:
    {{- range $key2, $value2 := $value.subjects }}
      - {{ $value2 }}
    {{- end }}
    storage: {{ $value.storage  }}
    replicas: {{ $value.replicas }}
    retention: {{ $value.retention }}
    maxAge: {{ $value.maxAge }}
{{- end }}
{{- else }}
  []
{{- end }}
keyValueConfigs:
{{- if .Values.keyValues }}
{{- range $key, $value := .Values.keyValues }}
  - name: {{ $value.name }}
    storage: {{ $value.storage  }}
    replicas: {{ $value.replicas }}
    timeToLiveInSeconds: {{ $value.timeToLiveInSeconds }}
    maxHistoryPerKey: {{ $value.maxHistoryPerKey }}
{{- end }}
{{- else }}
  []
{{- end }}
{{- end }}