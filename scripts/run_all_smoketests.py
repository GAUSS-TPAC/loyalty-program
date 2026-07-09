#!/usr/bin/env python3
"""Run every module smoke-test script against a local backend and summarize
pass/fail per module (workflow correctness check, one script per domain
module of the loyalty-program backend).

Usage: python3 scripts/run_all_smoketests.py [base_url]
"""
import subprocess
import sys
from pathlib import Path

BASE_URL = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8082"
SCRIPTS_DIR = Path(__file__).parent

MODULES = [
    ("loyalty", "local_smoketest.py"),
    ("wallet", "smoketest_wallet.py"),
    ("bonification", "smoketest_bonification.py"),
    ("tenant", "smoketest_tenant.py"),
    ("webhook", "smoketest_webhook.py"),
]


def main():
    results = []
    for module, script in MODULES:
        path = SCRIPTS_DIR / script
        print(f"\n{'=' * 70}\nMODULE: {module} ({script})\n{'=' * 70}")
        proc = subprocess.run([sys.executable, str(path), BASE_URL])
        results.append((module, proc.returncode == 0))

    print(f"\n{'=' * 70}\nRESUME\n{'=' * 70}")
    all_ok = True
    for module, ok in results:
        status = "OK" if ok else "ECHEC"
        print(f"  {module:15s} {status}")
        all_ok = all_ok and ok

    sys.exit(0 if all_ok else 1)


if __name__ == "__main__":
    main()
