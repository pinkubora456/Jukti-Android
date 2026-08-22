import sys

with open(sys.argv[1], 'rb') as f:
    data = f.read(64)
    print(" ".join(f"{b:02x}" for b in data))
