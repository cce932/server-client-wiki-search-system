# -*- coding: utf-8 -*
from bs4 import BeautifulSoup
from hanziconv import HanziConv
import requests
import re
import sys

def bot_get_google(question):
    url = 'https://www.google.com.tw/search?q=' + question + '+維基百科'
    headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'
                             ' AppleWebKit/537.36 (KHTML, like Gecko)'
                             ' Chrome/70.0.3538.102 Safari/537.36'}
    response = requests.get(url, headers)
    if response.status_code == 200:
        bs = BeautifulSoup(response.text, 'lxml')
        wiki_url = bs.find('div', class_="BNeawe UPmit AP7Wnd")
        if wiki_url==None:
            return None
        kwd = wiki_url.text.split('›')[-1]
        return str(kwd.strip())
    else:
        return None

def bot_get_wiki(keyword):
    r = ""
    retry = 5
    if keyword:
        response = requests.get('https://zh.wikipedia.org/wiki/' + keyword)
        bs = BeautifulSoup(response.text, 'lxml')
        p_list = bs.find_all('p')
        if p_list:
            for p in p_list:
                p = HanziConv.toTraditional(p.text)
                r += p
                if len(r) >100:
                    return str(r.strip())
                if len(p) < 0:
                    retry = retry - 1
                    if retry<1:
                        return None
        else:
            return None

key = bot_get_google(sys.argv[1])
content = bot_get_wiki(key) if key!=None else bot_get_wiki(sys.argv[1])
content = re.sub(r'\[[^\]]*\]', '', content) if content!=None else content
result = '查無資料' if content==None else content

print(result)
