import json
import random
import string
import requests
import os

# 1. توليد كود عشوائي جديد
random_chars = ''.join(random.choices(string.ascii_uppercase + string.digits, k=6))
new_code = f"ZONE-{random_chars}"
print(f"Generated new code: {new_code}")

# 2. إنشاء الملف النصي الذي سيحمله المستخدمون
file_name = "activation_code.txt"
with open(file_name, "w", encoding="utf-8") as text_file:
    text_file.write(f"مرحباً بك في ZoneStream!\n\nكود التفعيل الخاص بك للثلاثة أيام القادمة هو:\n{new_code}\n\nمشاهدة ممتعة!")

# 3. الرفع التلقائي إلى Up4ever
USERNAME = os.environ.get("UP4EVER_USERNAME")
PASSWORD = os.environ.get("UP4EVER_PASSWORD")
UPLOAD_URL = "https://www.up-4ever.net/cgi-bin/api.cgi"

download_link = "https://t.me/your_channel" # 🟢 رابط احتياطي (ضع رابط قناتك على تليجرام أو صفحتك هنا)

if USERNAME and PASSWORD:
    print("Uploading file to Up4ever...")
    data = {"op": "upload_api", "login": USERNAME, "password": PASSWORD}
    try:
        with open(file_name, "rb") as file_to_upload:
            files = {"file": file_to_upload}
            response = requests.post(UPLOAD_URL, data=data, files=files)
            if response.status_code == 200:
                result = response.json()
                if isinstance(result, list) and len(result) > 0:
                    download_link = result[0].get("file_link", download_link)
                    print(f"✅ File uploaded successfully! Link: {download_link}")
    except Exception as e:
        print(f"❌ Error during upload: {e}")

# 4. تحديث ملف التفعيل (activation.json) وحفظ الكود والرابط معاً
json_data = {
    "valid_code": new_code,
    "download_link": download_link
}
with open("activation.json", "w") as json_file:
    json.dump(json_data, json_file, indent=4)
