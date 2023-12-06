PlantCare App Citations:

The design for the Login & Signup pages was adapted from this YouTube video: https://www.youtube.com/watch?v=idbxxkF1l6k.

The design for the Main Activity was adapted from: https://www.geeksforgeeks.org/how-to-create-an-animated-splash-screen-in-android/amp/

All other implementations were carried out by following the lecture notes, demos, and the official Android Developer documentation: https://developer.android.com/docs.

=====================================================

The library used to create the calendar on the Calendar page: https://github.com/kizitonwose/Calendar

Prenual API used for care info, species, etc: https://perenual.com/docs/api

=====================================================

Notes on API usage:

We haven't purchased the unlimited requests plan, so the app is currently limited to making only 100 requests per day to the API. Each letter you type in the species text box triggers a call to the API, which means the allotted requests can be used up quickly.
For the info page, if the API calls have been exhausted, the care information sections will be empty. Additionally, the care info may also be empty if the species you've specified is not among the 3000 species available with the free api key.

