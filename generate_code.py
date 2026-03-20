import json
import random
import string
import requests
import os

# 1. توليد كود عشوائي
random_chars = ''.join(random.choices(string.ascii_uppercase + string.digits, k=6))
new_code = f"ZONE-{random_chars}"
print(f"Generated new code: {new_code}")

# 2. إنشاء الملف النصي (code.txt) في المجلد الرئيسي
code_file = "code.txt"
with open(code_file, "w", encoding="utf-8") as f:
    f.write(f"Your Activation Code is: {new_code}\n\nEnjoy with ZoneStream!")

# التأكد من وجود الملف لتجنب خطأ Git
if os.path.exists(code_file):
    print(f"✅ {code_file} created successfully.")

# 3. اختصار الرابط للربح عبر ShrinkMe
# استبدل 'LotfiDZ13' و 'Gamezone' ببياناتك الحقيقية
RAW_URL = f"https://raw.githubusercontent.com/LotfiDZ13/Gamezone/main/code.txt"
API_KEY = os.environ.get("SHORTENER_API_KEY")
final_link = RAW_URL 

if API_KEY:
    print("Shortening link via ShrinkMe.io...")
    api_url = f"https://www.shrinkme.io/api?api={API_KEY}&url={RAW_URL}"
    try:
        response = requests.get(api_url)
        res_data = response.json()
        if res_data.get("status") == "success":
            final_link = res_data.get("shortenedUrl")
            print(f"💰 Profitable Link: {final_link}")
        else:
            print(f"❌ API Error: {res_data.get('message')}")
    except Exception as e:
        print(f"❌ Connection error: {e}")

# 4. تحديث ملف activation.json
json_data = {
    "valid_code": new_code,
    "download_link": final_link
}
with open("activation.json", "w") as j:
    json.dump(json_data, j, indent=4)
