PROG7313 

POE Final 

ReadMe 

2025 

Team 2: 

Takudzwa Murwira – ST10392257 (Leader) 

Jason Daniel Isaacs – ST10039248 

Daniel Gorin – ST10438307 

Moegammad-Yaseen Salie – ST10257795 

 

  

GitHub Repository: 

https://github.com/DanielGorin/PROG7313-POE-Final-Submission---ST10392257---ST10039248---ST10438307---ST10257795.git  

Video Link: 

https://youtu.be/dLt3p9SAaaQ 

 

Running the project (Software): 

The project was developed and successfully ran on the following: 

Android Studio Meerkat Feature Drop | 2024.3.2 

Build #AI-243.25659.59.2432.13423653, built on April 29, 2025 

Runtime version: 21.0.6+-13368085-b895.109 amd64 

VM: OpenJDK 64-Bit Server VM by JetBrains s.r.o. 

 

Original Desing and Research Document: 

The original Design and Research Document submitted alongside this ReadMe provided a general contextual understanding of existing budgeting applications as well as a farmwork for a potential custom budgeting application. The original design created in that document was used as a foundation to create our final product and not as a strict design document.  

 

Application Purpose: 

The PocketChange app is designed to help users manage their personal finances by tracking monthly expenses, setting budget goals, and categorizing spending. Its goal is to promote mindful spending habits through visual insights (progress indicators, pie charts) and simple interaction. The app runs entirely offline using RoomDB, ensuring data privacy and local storage. 

Key features include: 

User-specific category management with custom icons/emojis. 
Expense tracking with detailed inputs (amount, time, description, photo). 
Monthly budget limits with visual feedback via a circular progress bar. 
A motivational “Splurge or Save” decision feature. 
Profile management with image support and dark mode toggle. 

 

GitHub Usage: 

This repository is hosted on GitHub to enable: 
Version control using Git for efficient collaboration and rollback. 
Issue tracking to document bugs, feature requests, and enhancements. 
Branch management for safe development, testing, and integration workflows. 
Backup & Sharing allowing seamless distribution and open-source contributions. 
GitHub Actions or CI/CD pipelines can be optionally integrated to automate testing and build verification if needed. 

 

Design Considerations: 

While designing PocketChange, the following considerations were prioritized: 

User Experience (UX): 

Simple navigation using a bottom navigation bar and scrollable layouts for clarity. Important features are easily accessible, like adding expenses and checking budget status. 

Visual Feedback: 

Use of CircularProgressIndicator and MPAndroidChart’s PieChart helps users immediately grasp their spending patterns and how close they are to budget limits. 

Responsiveness: 

Layouts are made scrollable and constraint-based to support different screen sizes. Buttons and cards use Material Design for a modern Android experience. 

  

Data Handling: 

Data is stored locally using RoomDB. Each user’s categories, expenses, budget, and profile image are associated with a userId to simulate multi-user support. 

Offline-first Design: 

The app does not rely on internet connectivity or Firebase; everything is stored and managed locally for full offline functionality. 

Expandable Architecture: 

The code is modular, making it easy to add features like Firebase Auth or cloud backup in the future if needed. 

 

 

 

2 Custom Functionalities Descriptions: 

Splurge Feature: 

Splurge Button – found on budget screen – Yellow circle containing a dice. The splurge button allows users to consult the application on unnecessary/luxury expenses. Cutting out all unnecessary expenses is extremely taxing and is only needed in the most dire of situations. Making these purchases (when financially responsible) reward users and helps to avoid burnout. This feature helps users make tough budgeting decisions. The user inputs a cost and then recommends to either save (not make the purchase) or splurge (make the purchase). The chance of either response is based on the ratio of cost to remaining budget. This takes the decision out of the user's hands reducing guilt and ensuring financial responsibility. 

Group 1, Grouped object 

Budgeting Tips Feature: 

Budgeting tip button – found on home screen – small lightbulb. Users can request a budgeting tip; this provides advice based on the user's budget performance. With three distinct levels Green = below budget minimum, Yellow = within budget, Red = over budget. The advice becomes increasingly extreme as users progress through the levels. This provides situational assistance and recommendations to users that are struggling to meet their budgets. 

Group 1, Grouped object 

 

 

  

Declaration: 

Intellectual Integrity Declarations 

We uphold academic honesty at the IIE. Students are expected to reflect on their academically honest practices when they submit assessments and are supported in their reflections when the institution directs them to consider the following: 

- Students are aware of assessment and intellectual integrity rules 

- Students behave in academically honest ways in all assessments 

- Students submit their own work 

- Students do not present the work of published resources as their own work 

- Students do not copy from other students, and do not let other students copy their work. 

- Students do not share work with other students 

- Students do not upload, nor download assessment questions and/ or responses to any website or App offering assessment assistance 

- Students do not use any AI tool without reviewing, re-writing, and re-working this information, and referencing any AI tools in their work. 

- Students include the output from any AI tool has been included in their assessment submissions as an annexure. 

- Students correctly cite sources of information 

- Students ensure that their referencing practices are technically correct, consistent and congruent. 

 
	

Daniel Gorin 
	

Takudzwa Murwira 
	

Jason Daniel Isaacs 
	

Moegammad-Yaseen Salie 

I have read the assessment rules provided in this declaration. 
	

DG 
	

TDM 
	

JDI 
	

MYS 

This assessment is my own work. 
	

DG 
	

TDM 
	

JDI 
	

MYS 

 

I have not copied any other student’s work in this assessment. 
	

DG 

 
	

TDM 
	

JDI 
	

MYS 

 

I have not uploaded the assessment question to any website or App offering assessment assistance. 
	

DG 

 
	

TDM 

 
	

JDI 
	

MYS 

 

I have not downloaded my assessment response from a website. 
	

DG 

 
	

TDM 

 
	

JDI 
	

MYS 

 

I have not used any AI tool without reviewing, re-writing, and re-working this information, and referencing any AI tools in my work. 
	

DG 

 
	

TDM 

 
	

JDI 
	

MYS 

 

I have not shared this assessment with any other student. 
	

DG 

 
	

TDM 

 
	

JDI 
	

MYS 

 

I have not presented the work of published sources as my own work. 
	

DG 

 
	

TDM 

 
	

JDI 
	

MYS 

 

I have correctly cited all my sources of information. 
	

DG 

 
	

TDM 

 
	

JDI 
	

MYS 

 

My referencing is technically correct, consistent, and congruent. 
	

DG 

 
	

TDM 

 
	

JDI 
	

MYS 

 

I have acted in an academically honest way in this assessment 
	

DG 

 
	

TDM 

 
	

JDI 
	

MYS 

 

 

Name: Daniel Gorin | Initial: DG | Student Number: ST10438307 | Date: 2025/06/09 

Name: Moegammad-Yaseen Salie | Initial: MYS| Student Number: ST10257795| Date: 2025/06/09 

Name: Takudzwa Denis Murwira| Initial: TDM| Student Number: st10392257| Date: 2025/06/09 

Name: Jason Daniel Isaacs | Initial: JDI| Student Number: ST10039248| Date: 2025/06/09 

 

  

Image References: 

Picture 1122998551, Picture 

Used as the image icon for the application (OpenAI, 2025) 

OpenAI, 2025. Sora [AI image generation system]. Available at: https://openai.com/sora [Accessed 1 June 2025]. 

 
Code References: 

Atukorala, S., 2021. How to use MPAndroidChart in Android Studio! [blog] Medium, 19 November. Available at: https://medium.com/@SeanAT19/how-to-use-mpandroidchart-in-android-studio-c01a8150720f  [Accessed 2025/05/28]. 

OpenAI, 2025. ChatGPT. [online] Available at: https://chatgpt.com/ [Accessed 21 Apr. 2025]. 

YouTube, 2021. Android Developers. [online video playlist] Available at: https://www.youtube.com/playlist?list=PLWz5rJ2EKKc8SmtMNw34wvYkqj45rV1d3 [Accessed 26 Apr. 2025]. 

YouTube, 2021. Stevdza-San. [online video playlist] Available at: https://www.youtube.com/playlist?list=PLSrm9z4zp4mEPOfZNV9O-crOhoMa0G2-o [Accessed 24 Apr. 2025]. 

 

 
