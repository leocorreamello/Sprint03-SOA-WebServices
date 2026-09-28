"""Gera o dashboard provisionado do Grafana a partir de consultas PromQL."""
import json
from pathlib import Path


DS = {"type": "prometheus", "uid": "prometheus"}


def query(expression: str, legend: str = "") -> dict:
    return {
        "datasource": DS,
        "editorMode": "code",
        "expr": expression,
        "legendFormat": legend,
        "range": True,
        "refId": "A",
    }


def stat(number: int, title: str, expression: str, x: int, *, unit: str = "short", thresholds=None, mappings=None) -> dict:
    return {
        "id": number,
        "title": title,
        "type": "stat",
        "datasource": DS,
        "gridPos": {"h": 5, "w": 6, "x": x, "y": 0},
        "targets": [query(expression)],
        "fieldConfig": {
            "defaults": {
                "unit": unit,
                "color": {"mode": "thresholds"},
                "thresholds": {"mode": "absolute", "steps": thresholds or [{"color": "green", "value": None}]},
                "mappings": mappings or [],
            },
            "overrides": [],
        },
        "options": {
            "colorMode": "value",
            "graphMode": "area",
            "justifyMode": "auto",
            "orientation": "auto",
            "reduceOptions": {"calcs": ["lastNotNull"], "fields": "", "values": False},
            "textMode": "auto",
        },
    }


def timeseries(number: int, title: str, expression: str, x: int, y: int, w: int, *, legend: str = "", unit: str = "short") -> dict:
    return {
        "id": number,
        "title": title,
        "type": "timeseries",
        "datasource": DS,
        "gridPos": {"h": 9, "w": w, "x": x, "y": y},
        "targets": [query(expression, legend)],
        "fieldConfig": {
            "defaults": {
                "unit": unit,
                "color": {"mode": "palette-classic"},
                "custom": {
                    "drawStyle": "line",
                    "lineInterpolation": "smooth",
                    "lineWidth": 2,
                    "fillOpacity": 24,
                    "showPoints": "never",
                    "spanNulls": False,
                    "axisPlacement": "auto",
                    "stacking": {"mode": "none", "group": "A"},
                },
            },
            "overrides": [],
        },
        "options": {
            "legend": {"displayMode": "list", "placement": "bottom", "showLegend": True},
            "tooltip": {"mode": "multi", "sort": "desc"},
        },
    }


panels = [
    stat(1, "DISPONIBILIDADE DA API", 'up{job="autointel"}', 0,
         thresholds=[{"color": "red", "value": None}, {"color": "green", "value": 1}],
         mappings=[{"options": {"0": {"text": "OFFLINE"}, "1": {"text": "ONLINE"}}, "type": "value"}]),
    stat(2, "REQUISIÇÕES · 5 MIN", "round(sum(increase(http_server_requests_seconds_count[5m])))", 6),
    stat(3, "401 + 403 · 5 MIN", 'round(sum(increase(http_server_requests_seconds_count{status=~"401|403"}[5m]))) or vector(0)', 12,
         thresholds=[{"color": "green", "value": None}, {"color": "orange", "value": 5}, {"color": "red", "value": 20}]),
    stat(4, "RATE LIMIT · 5 MIN", 'round(sum(increase(http_server_requests_seconds_count{status="429"}[5m]))) or vector(0)', 18,
         thresholds=[{"color": "green", "value": None}, {"color": "orange", "value": 1}, {"color": "red", "value": 10}]),
    timeseries(5, "Tráfego por código HTTP", "sum by (status) (rate(http_server_requests_seconds_count[1m]))", 0, 5, 14,
               legend="HTTP {{status}}", unit="reqps"),
    timeseries(6, "Latência p95", "histogram_quantile(0.95, sum by (le) (rate(http_server_requests_seconds_bucket[5m])))", 14, 5, 10,
               legend="p95", unit="s"),
    timeseries(7, "Falhas de autenticação e bloqueios", 'sum by (status) (increase(http_server_requests_seconds_count{status=~"401|403|429"}[5m]))', 0, 14, 14,
               legend="HTTP {{status}}"),
    timeseries(8, "Erros internos 5xx", 'sum(increase(http_server_requests_seconds_count{status=~"5.."}[5m])) or vector(0)', 14, 14, 10,
               legend="5xx / 5 min"),
]

dashboard = {
    "uid": "autointel-security",
    "title": "AutoIntel | Segurança da API",
    "description": "Ford Challenge · indicadores reais coletados pelo Prometheus a partir do Spring Boot Actuator",
    "tags": ["ford", "devsecops", "cybersecurity"],
    "timezone": "browser",
    "schemaVersion": 41,
    "version": 2,
    "refresh": "5s",
    "time": {"from": "now-15m", "to": "now"},
    "panels": panels,
    "annotations": {"list": []},
    "templating": {"list": []},
    "editable": False,
    "fiscalYearStartMonth": 0,
    "graphTooltip": 0,
    "links": [],
}

target = Path(__file__).with_name("grafana") / "autointel.json"
target.write_text(json.dumps(dashboard, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
print(target)
