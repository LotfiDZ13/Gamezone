import json
import random
import string
import requests
import os
import sys

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

download_link = "https://t.me/your_channel" 
upload_success = False

if USERNAME and PASSWORD:
    print(f"Attempting to upload to {UPLOAD_URL} with user: {USERNAME}")
    # بعض المواقع تتطلب usr و pwd بدلاً من login و password، سنقوم بإرسالهم معاً كإجراء احترازي
    data = {
        "op": "upload_api", 
        "login": USERNAME, 
        "password": PASSWORD,
        "usr": USERNAME,
        "pwd": PASSWORD
    }
    
    try:
        with open(file_name, "rb") as file_to_upload:
            # XFS API usually expects 'file' or 'file_0'
            files = {"file": file_to_upload, "file_0": file_to_upload}
            response = requests.post(UPLOAD_URL, data=data, files=files)
            
            print(f"Server Status: {response.status_code}")
            print(f"Raw Response from Up4ever: {response.text}") # 🟢 هذا السطر سيكشف لنا الخطأ
            
            if response.status_code == 200:
                try:
                    result = response.json()
                    if isinstance(result, list) and len(result) > 0:
                        download_link = result[0].get("file_link", download_link)
                        print(f"✅ File uploaded successfully! Link: {download_link}")
                        upload_success = True
                    else:
                        print("❌ Unexpected JSON format.")
                except ValueError:
                    print("❌ Response is not valid JSON. Up4ever returned HTML or error text.")
            else:
                print("❌ Server rejected the request.")
    except Exception as e:
        print(f"❌ Error during upload: {e}")
else:
    print("⚠️ Missing Username or Password in GitHub Secrets.")

# 4. تحديث ملف التفعيل
json_data = {
    "valid_code": new_code,
    "download_link": download_link
}
with open("activation.json", "w") as json_file:
    json.dump(json_data, json_file, indent=4)

# 5. إجبار جيتهاب على الفشل إذا لم يتم الرفع
if not upload_success:
    print("⚠️ Upload failed! Stopping the process so you can see the error.")
    sys.exit(1) # 🟢 هذا السطر سيجعل جيتهاب يظهر رسالة Failed ❌
