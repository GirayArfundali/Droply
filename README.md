Droply - Android App with Admin Panel Integration
---------------------------------------------------
This project is a comprehensive project, bridging the gap between mobile and web with Node.js. The mobile side of the project is powered by Android Studio, while the backend is powered by Visual Studio Code. My goal was to make instant sending and dynamic file sharing as easy and effective as possible.

Project Features
---------------------------------------------------
Push Notification System: When the app is opened, the device token provided by Firebase is automatically sent to the Node.js backend and stored in the database. This allows notifications to be sent through the admin panel even when the app is completely closed.

File Upload and Management: Files uploaded from the admin panel are included in the uploads section of the server-side (Node.js) files. To prevent file name truncation, when a file with the same name and common format is attempted to be uploaded, a number is automatically added to the file name (for example: report.pdf -> report(1).pdf).

WebView Integration: Seamlessly display the web panel within the mobile app.

Dynamic Content Listing: Uploaded files included in uploads are automatically listed in the web interface, enabling content management.

Technologies
---------------------------------------------------
Mobile: Kotlin, Android (Development Environment: Android Studio)

Backend: Node.js (Development Environment: Visual Studio Code)

API: Firebase Cloud Messaging (FCM)
--------------------------------------------------
https://github.com/user-attachments/assets/d8d7d1da-8dc9-4076-aa42-fb4c8de5e39b
