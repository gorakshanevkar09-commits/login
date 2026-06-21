from __future__ import annotations

import json
import shutil
from pathlib import Path
from typing import Any, Dict, List

import pandas as pd

BASE_DIR = Path(__file__).resolve().parent.parent
SERVER_DROP_DIR = BASE_DIR / "server_drop"
OUTPUT_DIR = BASE_DIR / "output"
DB_DIR = BASE_DIR / "db"
REPORT_FILE = OUTPUT_DIR / "report.xlsx"


def ensure_dirs() -> None:
    for directory in [SERVER_DROP_DIR, OUTPUT_DIR, DB_DIR]:
        directory.mkdir(parents=True, exist_ok=True)


def generate_csv(dest: Path, row_count: int = 5) -> Path:
    ensure_dirs()
    rows: List[Dict[str, Any]] = [
        {
            "order_id": f"ORD-{1000 + idx}",
            "customer": f"Customer {idx + 1}",
            "amount": 100 + idx * 10,
            "status": "NEW",
        }
        for idx in range(row_count)
    ]
    dest.parent.mkdir(parents=True, exist_ok=True)
    pd.DataFrame(rows).to_csv(dest, index=False)
    return dest


def drop_to_server(csv_path: Path) -> Path:
    ensure_dirs()
    destination = SERVER_DROP_DIR / csv_path.name
    shutil.copy2(csv_path, destination)
    return destination


def simulate_step_function(csv_path: Path) -> Path:
    df = pd.read_csv(csv_path)
    enriched: List[Dict[str, Any]] = []
    for row in df.to_dict(orient="records"):
        enriched.append(
            {
                **row,
                "processed_by": "demo-step-function",
                "verified": True,
            }
        )
    db_path = DB_DIR / "data.json"
    db_path.write_text(json.dumps(enriched, indent=2))
    return db_path


def verify_db(db_path: Path) -> bool:
    if not db_path.exists():
        return False
    data = json.loads(db_path.read_text())
    return isinstance(data, list) and len(data) > 0


def invoke_lambda(db_path: Path) -> Dict[str, Any]:
    data = json.loads(db_path.read_text())
    total_amount = sum(int(item.get("amount", 0)) for item in data)
    result = {
        "status": "SUCCESS",
        "recordCount": len(data),
        "totalAmount": total_amount,
    }
    lambda_output = OUTPUT_DIR / "lambda_result.json"
    lambda_output.write_text(json.dumps(result, indent=2))
    return result


def generate_excel_report(db_path: Path, report_path: Path = REPORT_FILE) -> Path:
    data = json.loads(db_path.read_text())
    report_path.parent.mkdir(parents=True, exist_ok=True)
    pd.DataFrame(data).to_excel(report_path, index=False)
    return report_path


def run_demo() -> Dict[str, Any]:
    ensure_dirs()
    csv_path = OUTPUT_DIR / "sample_data.csv"
    generated_csv = generate_csv(csv_path, row_count=5)
    server_csv = drop_to_server(generated_csv)
    db_path = simulate_step_function(server_csv)
    verified = verify_db(db_path)
    lambda_result = invoke_lambda(db_path)
    report_path = generate_excel_report(db_path)
    return {
        "csv_path": str(generated_csv),
        "server_path": str(server_csv),
        "db_path": str(db_path),
        "verification_passed": verified,
        "lambda_result": lambda_result,
        "report_path": str(report_path),
    }
