import json
import random

# توليد كود عشوائي مكون من 6 أرقام
new_code = str(random.randint(100000, 999999))

data = {
    "valid_code": new_code
}

# كتابة الكود الجديد في ملف activation.json
with open('activation.json', 'w') as f:
    json.dump(data, f, indent=4)

print(f"Successfully updated activation code to: {new_code}")
