import json, random, string, requests, os, hashlib

# 1. توليد الكود وبصمته (Hash)
random_chars = ''.join(random.choices(string.ascii_uppercase + string.digits, k=6))
new_code = f"ZONE-{random_chars}"
# تحويل الكود إلى MD5 لكي لا يظهر في ملف الـ JSON
code_hash = hashlib.md5(new_code.encode()).hexdigest()

# 2. تحديث الـ Secret Gist (المخفي)
GH_TOKEN = os.environ.get("GH_TOKEN")
# يمكنك إنشاء Gist يدوي أولاً ووضع ID الخاص به هنا، أو سيقوم السكربت بإنشاء واحد جديد
GIST_ID = "84b90ede16a82267dcf67e689faf564c" 

gist_content = f"Your Activation Code is: {new_code}"
headers = {"Authorization": f"token {GH_TOKEN}"}
gist_data = {"files": {"activation.txt": {"content": gist_content}}}

print("Updating Secret Gist...")
response = requests.patch(f"https://api.github.com/gists/{GIST_ID}", headers=headers, json=gist_data)
raw_url = response.json()['files']['activation.txt']['raw_url']

# 3. اختصار الرابط للربح
API_KEY = os.environ.get("SHORTENER_API_KEY")
final_link = raw_url
if API_KEY:
    short_res = requests.get(f"https://www.shrinkme.io/api?api={API_KEY}&url={raw_url}").json()
    if short_res.get("status") == "success":
        final_link = short_res.get("shortenedUrl")

# 4. تحديث activation.json (الذي يراه الناس)
# لن نضع الكود هنا، بل سنضع الـ Hash فقط!
json_data = {
    "code_hash": code_hash,
    "download_link": final_link
}
with open("activation.json", "w") as j:
    json.dump(json_data, j, indent=4)

print("🚀 Security System Active & Profitable!")
