"""
Scrape images from a text file of urls
"""
import argparse
import requests
import os

ap = argparse.ArgumentParser()
ap.add_argument("-u", "--urls", required=True, help="path to file containing image URLs")
ap.add_argument("-o", "--output", required=True, help="path to output directory of images")
ap.add_argument("-t", "--tag", required=False, help="class name to add to file names")
args = vars(ap.parse_args())

rows = open(args['urls'], 'r').read().strip().split('\n')
total = 0

filename = '{}.jpg'
if 'tag' in args:
    filename = args['tag'] + '_' + filename

for url in rows:
	try:
		r = requests.get(url, timeout=60)
		p = os.path.sep.join([args["output"], "{}.jpg".format(str(total).zfill(4))])
		f = open(p, 'wb')
		f.write(r.content)
		f.close()

		print('Downloaded {}'.format(p))
		total += 1
	except:
        print('Skipped {}'.format(p))

# Could do some verification on each image to check downloaded properly here #
