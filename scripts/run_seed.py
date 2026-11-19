import os
import subprocess
import sys

def load_env(env_path=".env"):
    if not os.path.exists(env_path):
        print(f"Warning: {env_path} not found")
        return
    with open(env_path, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if line and not line.startswith("#") and "=" in line:
                key, val = line.split("=", 1)
                clean_val = val.strip().strip('"').strip("'")
                os.environ[key.strip()] = clean_val

def main():
    load_env(".env")

    print(">>> Running ratings and watchlist generator...")
    res = subprocess.run([sys.executable, "scripts/generate_ratings_and_watchlist.py"])
    if res.returncode != 0:
        print("Error generating ratings and watchlist!")
        sys.exit(res.returncode)

    print("\nRatings and watchlist data generated successfully!")

if __name__ == "__main__":
    main()
