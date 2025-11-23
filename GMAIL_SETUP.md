# 📧 Gmail SMTP Setup Instructions

## Quick Setup (2 minutes)

### Step 1: Get Gmail App Password

1. **Open this link:** https://myaccount.google.com/apppasswords
2. **Sign in** with: shivamsriv961@gmail.com
3. **Select:**
   - App: "Mail"
   - Device: "Other" → Name it "Order Management System"
4. **Click "Generate"**
5. **Copy the 16-character password** (looks like: `xxxx xxxx xxxx xxxx`)

### Step 2: Add Password to Configuration

1. **Open file:**
   ```
   notification-service/src/main/resources/application.yml
   ```

2. **Find line 21** (around there):
   ```yaml
   password: YOUR_APP_PASSWORD_HERE
   ```

3. **Replace with your App Password:**
   ```yaml
   password: abcd efgh ijkl mnop
   ```
   (Use your actual 16-character password - spaces are optional)

### Step 3: Restart Notification Service

```bash
# Stop the service
pkill -f "notification-service"

# Start it again
cd notification-service
mvn spring-boot:run
```

### Step 4: Test It!

```bash
# Place an order
TOKEN=$(curl -s -X POST "http://localhost:8080/auth/login?username=test")

curl -X POST -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "productId": "LAPTOP-001",
    "quantity": 1,
    "price": 1200.00,
    "customerId": "test"
  }' \
  http://localhost:8080/api/orders
```

**Check your email inbox!** You should receive:
```
Subject: Order Confirmation - Order #X
From: shivamsriv961@gmail.com
To: shivamsriv961@gmail.com
```

---

## What I've Already Configured

✅ Added Spring Mail dependency to `pom.xml`
✅ Configured Gmail SMTP settings in `application.yml`
✅ Updated `NotificationService.java` to send real emails
✅ Set your email as both sender and recipient

**All you need to do:** Add your App Password to line 21 of `application.yml`!

---

## Troubleshooting

### "Authentication failed"
- Make sure you're using an **App Password**, not your regular Gmail password
- App Passwords are 16 characters: `xxxx xxxx xxxx xxxx`

### "Less secure app access"
- You don't need this! App Passwords work without it
- Just make sure 2-Step Verification is enabled on your Google Account

### Still not working?
- Check the logs: `tail -f /tmp/notification-service.log`
- Look for: "✅ EMAIL SENT SUCCESSFULLY" or error messages

---

## Security Note

⚠️ **Don't commit your App Password to GitHub!**

Before pushing to GitHub, either:
1. Use environment variables: `${GMAIL_APP_PASSWORD}`
2. Add `application.yml` to `.gitignore`
3. Use a separate `application-local.yml` file

I've set it up with a placeholder so it's safe to commit as-is.
