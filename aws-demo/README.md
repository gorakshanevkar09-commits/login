# AWS Demo Project

This is a demo Python project that simulates an AWS-style data flow:

1. Generate a CSV file
2. Drop the CSV into a simulated server location
3. Simulate an AWS Step Function execution to process and store data in a mock database
4. Verify the database content
5. Simulate an AWS Lambda function to summarize data
6. Generate an Excel report from the processed data

## Requirements

- Python 3.10 or later
- pip

## Setup

```powershell
cd c:\Dev\aws-demo
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
```

## Run Demo

```powershell
python run_demo.py
```

## Run Tests

```powershell
pytest
```

## Project Structure

- `aws_demo/demo.py` - core demo flow logic
- `run_demo.py` - simple runner script
- `tests/test_demo.py` - pytest test coverage for the demo flow
- `requirements.txt` - Python dependencies
