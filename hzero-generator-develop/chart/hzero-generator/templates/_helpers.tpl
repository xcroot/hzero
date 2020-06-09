{{/* vim: set filetype=mustache: */}}
{{- /*
service.labels.standard prints the standard service Helm labels.
The standard labels are frequently used in metadata.
*/ -}}

{{- define "service.microservice.labels" -}}
choerodon.io/version: {{ .Chart.Version | quote }}
choerodon.io/service: {{ .Chart.Name | quote }}
{{- end -}}

{{- define "service.labels.standard" -}}
choerodon.io/release: {{ .Release.Name | quote }}
{{- end -}}

{{- define "service.match.labels" -}}
choerodon.io/release: {{ .Release.Name | quote }}
{{- end -}}

{{- define "service.logging.deployment.label" -}}
choerodon.io/logs-parser: {{ .Values.logs.parser | quote }}
{{- end -}}