param(
    [ValidateSet("apply", "verify", "cleanup")]
    [string]$Action = "verify"
)

$Namespace = "cloud-native-platform"
$Manifest = Join-Path $PSScriptRoot "chaos-mesh.yaml"

switch ($Action) {
    "apply" {
        kubectl apply -f $Manifest
    }
    "cleanup" {
        kubectl delete -f $Manifest --ignore-not-found
    }
    "verify" {
        kubectl -n $Namespace rollout status deployment/platform-api --timeout=120s
        kubectl -n $Namespace rollout status deployment/platform-worker --timeout=120s
        kubectl -n $Namespace get pods
        kubectl -n $Namespace get hpa
    }
}
