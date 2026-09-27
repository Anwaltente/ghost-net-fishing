"""
Automatisierter Oberflächentest (End to End) für Ghost Net Fishing mit Playwright.

Voraussetzung: Anwendung läuft mit frischer Datenbank (Beispieldaten aktiv).
Aufruf:  pip install playwright && playwright install chromium
         python test/e2e_test.py [BASIS_URL] [SCREENSHOT_ORDNER]
"""
import os, sys
from playwright.sync_api import sync_playwright
B=sys.argv[1] if len(sys.argv)>1 else "http://localhost:8080/ghost-net-fishing"
S=(sys.argv[2] if len(sys.argv)>2 else "screenshots")+"/"
os.makedirs(S, exist_ok=True)
def shot(p,name): p.screenshot(path=S+name+".png", full_page=True); print("shot",name)
with sync_playwright() as pw:
    br=pw.chromium.launch()
    ctx=br.new_context(viewport={"width":1280,"height":800}, device_scale_factor=1.5, locale="de-DE")
    p=ctx.new_page()
    p.on("dialog", lambda d: d.accept())
    p.goto(B+"/"); shot(p,"01_start")
    p.click("text=Zu bergende Netze"); p.wait_for_load_state(); shot(p,"02_netze_gast")
    # Melden: Validierung
    p.click("nav >> text=Netz melden"); p.wait_for_load_state(); shot(p,"03_melden_leer")
    p.fill("#meldeForm\\:breitengrad","95"); p.fill("#meldeForm\\:laengengrad","abc")
    p.click("#meldeForm\\:melden"); p.wait_for_load_state(); shot(p,"04_melden_validierung")
    # anonyme Meldung
    p.fill("#meldeForm\\:breitengrad","54.3233"); p.fill("#meldeForm\\:laengengrad","10.1228"); p.fill("#meldeForm\\:groesse","35")
    p.fill("#meldeForm\\:bemerkung","Vor dem Kieler Leuchtturm, blaues Stellnetz.")
    p.click("#meldeForm\\:melden"); p.wait_for_load_state(); shot(p,"05_melden_anonym_erfolg")
    assert "anonym gemeldet" in p.content()
    # namentliche Meldung
    p.click("nav >> text=Netz melden"); p.wait_for_load_state()
    p.uncheck("#meldeForm\\:anonym"); p.wait_for_selector("#meldeForm\\:name")
    p.fill("#meldeForm\\:breitengrad","54.0931"); p.fill("#meldeForm\\:laengengrad","7.9014"); p.fill("#meldeForm\\:groesse","150")
    p.fill("#meldeForm\\:name","Tom Berger"); p.fill("#meldeForm\\:telefon","+49 151 1112223")
    shot(p,"06_melden_namentlich")
    p.click("#meldeForm\\:melden"); p.wait_for_load_state()
    assert "wurde unter der Nr." in p.content()
    # Anmelden
    p.click("text=Anmelden"); p.wait_for_load_state()
    p.click("#anmeldeForm\\:anmelden"); p.wait_for_load_state(); shot(p,"07_anmelden_validierung")
    p.fill("#anmeldeForm\\:name","Lena Vogt"); p.fill("#anmeldeForm\\:telefon","+49 152 9876543")
    shot(p,"08_anmelden")
    p.click("#anmeldeForm\\:anmelden"); p.wait_for_load_state(); shot(p,"09_netze_angemeldet")
    # Bergung uebernehmen (erste Zeile mit Knopf)
    p.locator("tr", has_text="Helgoland").locator("text=Bergung übernehmen").click(); p.wait_for_load_state()
    shot(p,"10_bergung_uebernommen"); assert "übernommen" in p.content()
    # Filter
    p.select_option("#netzForm\\:filter","BERGUNG_BEVORSTEHEND"); p.wait_for_timeout(800); shot(p,"11_filter_bevorstehend")
    # Konkurrenz: zweite Person
    ctx2=br.new_context(viewport={"width":1280,"height":800}, device_scale_factor=1.5)
    q=ctx2.new_page(); q.goto(B+"/anmelden.xhtml")
    q.fill("#anmeldeForm\\:name","Jens Petersen"); q.fill("#anmeldeForm\\:telefon","+49 171 2345678"); q.click("#anmeldeForm\\:anmelden"); q.wait_for_load_state()
    p.goto(B+"/netze.xhtml")
    row_q=q.locator("tr", has_text="Palma"); row_q.wait_for()
    p.locator("tr", has_text="Palma").locator("text=Bergung übernehmen").click(); p.wait_for_load_state()
    row_q.locator("text=Bergung übernehmen").click(); q.wait_for_load_state()
    shot(q,"12_konflikt_bereits_vergeben"); assert "kann nicht übernommen werden" in q.content()
    # Meine Bergungen
    p.click("nav >> text=Meine Bergungen"); p.wait_for_load_state(); shot(p,"13_meine_bergungen")
    p.locator("#bergungForm\\:bevorstehend tr", has_text="Helgoland").locator("text=Als geborgen melden").click(); p.wait_for_load_state()
    shot(p,"14_geborgen_gemeldet"); assert "als geborgen gemeldet" in p.content()
    # Verschollen melden
    p.click("nav >> text=Zu bergende Netze"); p.wait_for_load_state()
    p.locator("tr", has_text="Doggerbank").locator("input[value='Verschollen']").click(); p.wait_for_load_state()
    shot(p,"15_verschollen_gemeldet"); assert "als verschollen gemeldet" in p.content()
    # fremde Bergung kann nicht als geborgen gemeldet werden: Jens sieht bei Palma keinen Geborgen-Knopf
    q.goto(B+"/netze.xhtml"); 
    assert q.locator("tr", has_text="Palma").locator("text=Als geborgen melden").count()==0
    # Startseite nach Aenderungen
    p.click("nav >> text=Start"); p.wait_for_load_state(); shot(p,"16_start_nachher")
    # Abmelden
    p.click("text=Abmelden"); p.wait_for_load_state(); shot(p,"17_abgemeldet")
    p.goto(B+"/meine-bergungen.xhtml"); shot(p,"18_meine_bergungen_gast")
    br.close()
print("ALLE TESTS OK")
