from aws_demo.demo import run_demo


def main() -> None:
    summary = run_demo()
    print("=== AWS Demo Automation Summary ===")
    for key, value in summary.items():
        print(f"{key}: {value}")


if __name__ == "__main__":
    main()
