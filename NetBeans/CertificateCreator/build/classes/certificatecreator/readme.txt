Certificate Creator Revision 1:



NOTICE: Certificate Creator will generate certificates in the directory it is initialized from. If the user wishes to move Certificate Creator to a different directory, close the application, delete the generated resources folder, move the "CertificateCreator.jar" file to the desired directory, and reinitialize Certificate Creator.



General:
Certificate Creator generates student certificates automatically using a user-specified stuent list and .docx template. All program functionality is detailed in order of chronological steps the user should take to properly set up Certificate Creator on first use.

On first use, Certificate Creater generates a resources folder in the directory it is executed from. While not required, it is recommended that the user store any and all word templates and student lists in this folder. The "data.properties" file should be ignored. Any user-made changes may corrupt Certificate Creator. Should this occur, deleting the "data.properties" file should resolve any issues.



Set Student List:
The set student list button opens a windows explorer which the user can use to choose the desired student list. Doing so will restart Certificate Creator and the list on the right side of Certificate Creator's home window should populate with names. 

Certificate Creator is designed to take exported text (.txt) files from PowerSchool as student lists. Other files will work provided that the first line is a header and the list of students is divided into at least two columns with "First_Name" and "Last_Name" as the first rows in each column.

Certificate Creator looks for "First_Name" and "Last_Name" explicitly, so when a list is exported from PowerSchools, as many columns in any order can be included in the file used by Certificate Creator.



Set Certificate Template:
The "Set Certificate Template" button opens a windows explorer which the user can use to choose the desired certificate template. Only word documents written using xml (.docx) will function as a template. If another file type is selected, the user will be notified (See "Generate Certificate" section). When creating a template, ensure margins are set to the default of 1 inch and the page orientation is set to landscape. Additionally, all text should be removed from the template. Font, font size, indentation, and text content cannot be changed by the user at this point.



Specify Student:
After the student list and certificate template are set and the student list is properly populated, the user can either scroll through the list to find the desired student or begin typing the desired name into the "Student Name" field. As the user types, the list will filter names that match the letters typed.



Generate Certificate:
Once the user has found the desired student, clicking the name in the list once will prompt the user to confirm certificate generation. If the file already exists, the user will be prompted to overwrite it. Once written, certificate can be found in the same folder as Certificate Creator.