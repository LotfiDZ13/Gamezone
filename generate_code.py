import json, random, string, requests, os, hashlib, sys

# 1. توليد الكود وبصمته
random_chars = ''.join(random.choices(string.ascii_uppercase + string.digits, k=6))
new_code = f"ZONE-{random_chars}"
code_hash = hashlib.md5(new_code.encode()).hexdigest()

# 2. إعدادات جيتهاب
GH_TOKEN = os.environ.get("GH_TOKEN")
GIST_ID = "ضع_هنا_ID_الخاص_بك_فقط" # تأكد أنه الـ ID المكون من حروف وأرقام فقط

if not GH_TOKEN or "ضع_هنا" in GIST_ID:
    print("❌ Error: GH_TOKEN or GIST_ID is missing!")
    sys.exit(1)

gist_content = f"Your Activation Code is: {new_code}"
headers = {"Authorization": f"token {GH_TOKEN}", "Accept": "application/vnd.github.v3+json"}
# سنقوم بتحديث أي ملف موجود في الـ Gist أو إنشاء ملف جديد
gist_data = {"files": {"activation.txt": {"content": gist_content}}}

print(f"Attempting to update Gist ID: {GIST_ID}...")

try:
    response = requests.patch(f"https://api.github.com/gists/{GIST_ID}", headers=headers, json=gist_data)
    
    if response.status_code == 200:
        raw_url = response.json()['files']['activation.txt']['raw_url']
        print(f"✅ Gist updated! Raw URL: {raw_url}")
    else:
        print(f"❌ GitHub API Error {response.status_code}: {response.text}")
        sys.exit(1)
except Exception as e:
    print(f"❌ Connection Error: {e}")
    sys.exit(1)

# 3. اختصار الرابط للربح
API_KEY = os.environ.get("SHORTENER_API_KEY")
final_link = raw_url
if API_KEY:
    try:
        short_res = requests.get(f"https://www.shrinkme.io/api?api={API_KEY}&url={raw_url}").json()
        if short_res.get("status") == "success":
            final_link = short_res.get("shortenedUrl")
            print(f"💰 Link Shortened: {final_link}")
    except:
        print("⚠️ Shortening failed, using raw URL.")

# 4. تحديث activation.json
json_data = {"code_hash": code_hash, "download_link": final_link}
with open("activation.json", "w") as j:
    json.dump(json_data, j, indent=4)

print("🚀 Script finished successfully!")
