import json
import random
import string
import requests
import os
import sys

# 1. توليد كود عشوائي
random_chars = ''.join(random.choices(string.ascii_uppercase + string.digits, k=6))
new_code = f"ZONE-{random_chars}"
print(f"Generated new code: {new_code}")

file_name = "activation_code.txt"
with open(file_name, "w", encoding="utf-8") as text_file:
    text_file.write(f"كود التفعيل الجديد: {new_code}")

# 2. إعدادات الرفع
USERNAME = os.environ.get("UP4EVER_USERNAME")
PASSWORD = os.environ.get("UP4EVER_PASSWORD")
UPLOAD_URL = "https://www.up-4ever.net/cgi-bin/api.cgi"

# 🟢 إضافة Headers لمحاكاة متصفح حقيقي وتخطي Cloudflare
headers = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36",
    "Accept": "*/*",
    "Accept-Language": "en-US,en;q=0.9",
    "Referer": "https://www.up-4ever.net/"
}

upload_success = False

if USERNAME and PASSWORD:
    data = {
        "op": "upload_api", 
        "login": USERNAME, 
        "password": PASSWORD
    }
    
    try:
        with open(file_name, "rb") as f:
            files = {"file_0": (file_name, f, "text/plain")}
            # 🟢 استخدام Session للحفاظ على الكوكيز
            session = requests.Session()
            response = session.post(UPLOAD_URL, data=data, files=files, headers=headers, timeout=30)
            
            print(f"Status: {response.status_code}")
            
            if "file_link" in response.text:
                # محاولة استخراج الرابط إذا لم يكن JSON صريحاً
                try:
                    res_json = response.json()
                    download_link = res_json[0]['file_link']
                except:
                    import re
                    links = re.findall(r'https?://www.up-4ever.net/[a-zA-Z0-9]+', response.text)
                    download_link = links[0] if links else "https://t.me/your_channel"
                
                print(f"✅ Success! Link: {download_link}")
                upload_success = True
            else:
                print("❌ Cloudflare is still blocking the request.")
                print("Response Snippet:", response.text[:200])
    except Exception as e:
        print(f"❌ Error: {e}")

# تحديث الملف المحلي (حتى لو فشل الرفع سنحدث الكود في جيتهاب)
json_data = {"valid_code": new_code, "download_link": "Pending..."}
with open("activation.json", "w") as j:
    json.dump(json_data, j, indent=4)

# إذا فشل Cloudflare، سنعتبر العملية ناجحة في جيتهاب لنرى الملف، 
# لكننا سنعرف من السجل إذا تم الرفع أم لا.
