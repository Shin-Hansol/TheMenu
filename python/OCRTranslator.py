import cv2
import pytesseract
from PIL import Image
import os
import matplotlib.pyplot as plt
from googletrans import Translator

pytesseract.pytesseract.tesseract_cmd = r'C:\Program Files\Tesseract-OCR\tesseract'

sourceLang = "eng"
destLang = "ko"
inputFileName = "Restaurant"

imageFileName = "image\{}.jpg".format(inputFileName)
image = cv2.imread(imageFileName, cv2.IMREAD_COLOR)

gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
gray = cv2.threshold(gray, 0, 255, cv2.THRESH_BINARY | cv2.THRESH_OTSU)[1]
#kernel_size = 5
#gray = cv2.medianBlur(gray, kernel_size)

tempFileName = "temp\{}.png".format(inputFileName)
cv2.imwrite(tempFileName, gray)

text = pytesseract.image_to_string(Image.open(tempFileName), lang=sourceLang)
os.remove(tempFileName)

textFileName = "result\{}2.txt".format(inputFileName)
with open(textFileName, 'w', encoding='utf-8') as f :
    f.write(text)

#cv2.imshow("Image", image)
#cv2.imshow("Gray", gray)
#cv2.waitKey(0)

translator = Translator()

result = translator.translate(text, dest=destLang)

resultFileName = "result\{}.txt".format(inputFileName)
with open(resultFileName, 'w', encoding='utf-8') as f :
    f.write(result.text)
