# Overview



Open source UI automation testing framework based on Webdriver/Appium, TestNG/Junit, with maven integration. 

* Unifies mobile and web testing, using a common, version controlled code base \(Automation Core\)
* Each testing project is treated as a client for the Automation Core, meaning one central code base for all UI testing projects
* A client can have multiple test projects, as well as multiple platforms \(web, Android, iOS, Win\), associated with it.
* Modular design. Each project/component is treated as a module,  fully capable of interacting with one another. This allows for multi component and multiplatform testing. Eg. Create user through component A \(API\), validate in component B \(web\), do action in component C \(Android\), validate results in component D \(iOS\)
* All interaction with the UI are through utility functions called Helpers, which are stable and optimized set of wrapper functions, removing inconsistencies in coding styles and enforcing testing best practices 
* Integrates seamlessly with the API testing framework for end to end testing
* Detailed reports through ExtentTest reports 

