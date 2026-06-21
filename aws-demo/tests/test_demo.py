import json
from pathlib import Path

import pandas as pd

from aws_demo.demo import (
    drop_to_server,
    generate_csv,
    generate_excel_report,
    invoke_lambda,
    run_demo,
    simulate_step_function,
    verify_db,
)


def test_generate_csv(tmp_path: Path) -> None:
    csv_file = tmp_path / "sample.csv"
    result = generate_csv(csv_file, row_count=3)

    assert result.exists()
    data = pd.read_csv(result)
    assert len(data) == 3
    assert list(data.columns) == ["order_id", "customer", "amount", "status"]


def test_demo_flow(tmp_path: Path, monkeypatch) -> None:
    demo_root = tmp_path / "aws-demo"
    demo_root.mkdir()

    monkeypatch.setattr("aws_demo.demo.BASE_DIR", demo_root)
    monkeypatch.setattr("aws_demo.demo.SERVER_DROP_DIR", demo_root / "server_drop")
    monkeypatch.setattr("aws_demo.demo.OUTPUT_DIR", demo_root / "output")
    monkeypatch.setattr("aws_demo.demo.DB_DIR", demo_root / "db")

    demo_summary = run_demo()
    assert demo_summary["verification_passed"] is True
    assert demo_summary["lambda_result"]["status"] == "SUCCESS"
    assert Path(demo_summary["csv_path"]).exists()
    assert Path(demo_summary["report_path"]).exists()

    db_content = json.loads(Path(demo_summary["db_path"]).read_text())
    assert isinstance(db_content, list)
    assert len(db_content) == 5
