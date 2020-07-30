"""
This script move a folder all images with name format ("{id}_xxx.WSQ.bmp") into "\{id}\"

"""
import os, glob

directory = dict()
all_paths = glob.glob("**.bmp*")

for path in all_paths:
    key = path.split("_")[0]
    directory.setdefault(key, list())
    directory[key].append(path)


for k,v in directory.items():
    new_k = k[1:].lstrip("0")
    os.mkdir(new_k)
    for vv in v:
        name = vv.split("_")[1][:2]
        os.rename(vv, "{}\{}".format(new_k,name))