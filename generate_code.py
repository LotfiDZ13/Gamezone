import json
import random
import string
import requests
import os

# 1. توليد كود عشوائي جديد
random_chars = ''.join(random.choices(string.ascii_uppercase + string.digits, k=6))
new_code = f"ZONE-{random_chars}"
print(f"Generated new code: {new_code}")

# 2. تحديث ملف التفعيل (activation.json)
data = {"valid_code": new_code}
with open("activation.json", "w") as json_file:
    json.dump(data, json_file, indent=4)

# 3. إنشاء الملف النصي الذي سيحمله المستخدمون
file_name = "activation_code.txt"
with open(file_name, "w", encoding="utf-8") as text_file:
    text_file.write(f"Welcome to ZoneStream!\n\nYour Activation Code for the next 3 days is:\n{new_code}\n\nEnjoy watching!")

# ==========================================
# 4. الرفع التلقائي إلى Up4ever (بدون API Key)
# ==========================================
USERNAME = os.environ.get("UP4EVER_USERNAME")
PASSWORD = os.environ.get("UP4EVER_PASSWORD")

UPLOAD_URL = "https://www.up-4ever.net/cgi-bin/api.cgi"

if USERNAME and PASSWORD:
    print("Uploading file to Up4ever...")
    
    # استخدام اسم المستخدم وكلمة المرور مباشرة
    data = {
        "op": "upload_api",
        "login": USERNAME,
        "password": PASSWORD,
    }
    
    try:
        with open(file_name, "rb") as file_to_upload:
            files = {"file": file_to_upload}
            response = requests.post(UPLOAD_URL, data=data, files=files)
            
            if response.status_code == 200:
                result = response.json()
                if isinstance(result, list) and len(result) > 0:
                    download_link = result[0].get("file_link")
                    print(f"✅ File uploaded successfully!")
                    print(f"📥 Download link: {download_link}")
                else:
                    print(f"Unexpected response format: {response.text}")
            else:
                print(f"❌ Failed to upload. Status code: {response.status_code}")
    except Exception as e:
        print(f"❌ Error during upload: {e}")
else:
    print("⚠️ UP4EVER_USERNAME or UP4EVER_PASSWORD is not set in environment variables.")
