"""
For converting VGG Image Annotator JSON files to more readable bounding box CSV files
"""
import argparse
import json
import re
import csv

ap = argparse.ArgumentParser()
ap.add_argument("input", nargs=1, help="Input JSON file")
ap.add_argument("output", nargs=1, help="Output CSV file")

args = vars(ap.parse_args())

data = json.load(open(args['input'][0]))
outfile = open(args['output'][0], 'w')
writer = csv.writer(outfile)

for k in data:
	file_name=data[k]['filename']

	class_name = re.search(r".*(?=_0)", file_name).group()
	for i in data[k]['regions']:
		shape = data[k]['regions'][i]['shape_attributes']
		x1 = shape['x']
		y1 = shape['y']
		x2 = x1 + shape['width']
		y2 = y1 + shape['height']
		
		writer.writerow([file_name, x1, y1, x2, y2, class_name])

outfile.close()		
