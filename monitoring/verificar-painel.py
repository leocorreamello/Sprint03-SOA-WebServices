"""Confere se cada consulta provisionada retorna séries no Prometheus local."""
import json
from pathlib import Path
from urllib.parse import urlencode
from urllib.request import urlopen

dashboard = json.loads((Path(__file__).parent / "grafana" / "autointel.json").read_text(encoding="utf-8"))
for panel in dashboard["panels"]:
    expression = panel["targets"][0]["expr"]
    url = "http://localhost:19090/api/v1/query?" + urlencode({"query": expression})
    with urlopen(url, timeout=10) as response:
        result = json.load(response)
    values = result["data"]["result"]
    print(f"{panel['id']:>2} {panel['title']}: {len(values)} séries; status={result['status']}")
