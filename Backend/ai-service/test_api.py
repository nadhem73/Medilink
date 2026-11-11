import httpx
resp = httpx.get("http://localhost:8087/api/bilans/doctor?page=0&size=50", headers={"X-User-Id": "2"}, timeout=5)
if resp.status_code == 200:
    data = resp.json()
    print(f"Remaining: {data.get('totalElements')}")
    for b in data.get("content", []):
        print(f"  {b['id']}")
else:
    print(resp.status_code, resp.text[:200])
