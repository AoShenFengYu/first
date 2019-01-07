#conding=utf8
import os
import re
import sys
mainpath = "/Users/xm180319/stickerapk"
versioncode = '7'
changeCode = True
g = os.walk(mainpath)

for path,dir_list,file_list in g:
    for file_name in file_list:
        if file_name.startswith("File"):
            filepath = os.path.join(path, file_name)
            filepathnew = os.path.join(path,file_name.lower())
            print("fileName=" + filepath+';new='+filepathnew)
            os.rename(filepath, filepathnew)
        elif file_name.startswith("config") and changeCode:
            p = path+"/"+file_name
            with open(p , "r") as f:
                config = f.read()
            with open(p, 'w') as f:
                if len(sys.argv) > 1:
                    versioncode = sys.argv[1]
                vc = "version_code:"+ versioncode
                text = re.sub(r'version_code\s*:\s*\d+', vc, config)
                print(text)
                f.write(text)


g = os.walk(mainpath)
for path,dir_list,file_list in g:
    for dir_name in dir_list:
        if(path == mainpath):
            bundle = os.path.join(path, dir_name)
            print(bundle)
            assemble_cmd = "./gradlew -Pbundle="+bundle+" aR"
            os.system(assemble_cmd)
