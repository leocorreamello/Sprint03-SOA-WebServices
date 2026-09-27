#!/usr/bin/env python3
"""Gera docs/evidencias/RESULTADO_TESTES.md a partir dos relatórios do Surefire e do JaCoCo.
Uso: ./mvnw verify && python3 scripts/gerar-evidencias.py
"""
import csv
import glob
import platform
import subprocess
import xml.etree.ElementTree as ET
from datetime import datetime
from pathlib import Path

raiz = Path(__file__).resolve().parent.parent
suites = []
for arquivo in sorted(glob.glob(str(raiz / "target/surefire-reports/TEST-*.xml"))):
    s = ET.parse(arquivo).getroot()
    casos = []
    for c in s.findall("testcase"):
        falhou = c.find("failure") is not None or c.find("error") is not None
        pulado = c.find("skipped") is not None
        casos.append((c.get("name"), float(c.get("time", 0)), "❌ FALHOU" if falhou else "⏭️ IGNORADO" if pulado else "✅ OK"))
    classe = Path(arquivo).stem.removeprefix("TEST-")
    suites.append((s.get("name"), classe, int(s.get("tests")),
                   int(s.get("failures")) + int(s.get("errors")), int(s.get("skipped")), float(s.get("time")), casos))

total = sum(x[2] for x in suites)
falhas = sum(x[3] for x in suites)
pulados = sum(x[4] for x in suites)

cobertura = {}
csv_jacoco = raiz / "target/site/jacoco/jacoco.csv"
if csv_jacoco.exists():
    linhas = list(csv.DictReader(open(csv_jacoco)))
    for k, rotulo in [("INSTRUCTION", "Instruções"), ("LINE", "Linhas"), ("BRANCH", "Branches"), ("METHOD", "Métodos")]:
        m = sum(int(r[k + "_MISSED"]) for r in linhas)
        c = sum(int(r[k + "_COVERED"]) for r in linhas)
        cobertura[rotulo] = f"{100 * c / (m + c):.1f}% ({c}/{m + c})"

java = subprocess.run(["java", "-version"], capture_output=True, text=True).stderr.splitlines()[0]
comando = ".\\mvnw.cmd -q verify" if platform.system() == "Windows" else "./mvnw verify"
out = [
    "# Evidência de execução dos testes automatizados", "",
    f"- **Data da execução:** {datetime.now():%d/%m/%Y %H:%M}",
    f"- **Ambiente:** {platform.system()} {platform.release()} · {java}",
    f"- **Comando:** `{comando}`", "",
    "## Resumo", "",
    "| Total | Sucesso | Falhas | Ignorados |", "|---:|---:|---:|---:|",
    f"| {total} | {total - falhas - pulados} | {falhas} | {pulados} |", "",
]
if cobertura:
    out += ["## Cobertura de código (JaCoCo)", "", "| Métrica | Cobertura |", "|---|---:|"]
    out += [f"| {k} | {v} |" for k, v in cobertura.items()]
    out += ["", "Relatório completo: `target/site/jacoco/index.html` · Relatório HTML dos testes: `target/reports/surefire.html`", ""]
out += ["## Resultado por suíte", "", "| Suíte | Classe | Testes | Falhas | Tempo (s) |", "|---|---|---:|---:|---:|"]
for nome, classe, t, f, _, tempo, _ in suites:
    out.append(f"| {nome} | `{classe.split('.')[-1]}` | {t} | {f} | {tempo:.2f} |")
out.append("")
out.append("## Detalhamento dos cenários")
for nome, classe, _, _, _, _, casos in suites:
    out += ["", f"### {nome}", "", "| Cenário | Resultado | Tempo (s) |", "|---|---|---:|"]
    out += [f"| {n.replace('|', '/')} | {r} | {t:.3f} |" for n, t, r in casos]
out.append("")

destino = raiz / "docs/evidencias/RESULTADO_TESTES.md"
destino.parent.mkdir(parents=True, exist_ok=True)
destino.write_text("\n".join(out), encoding="utf-8")
print(f"{destino.relative_to(raiz)}: {total} testes, {falhas} falhas")
