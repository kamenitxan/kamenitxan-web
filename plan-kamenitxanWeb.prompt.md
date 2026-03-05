# Plán: Sebeprezentační web – kamenitxan.eu

Stávající Jakon/Scala šablonový projekt bude přeměněn na plnohodnotný sebeprezentační web. Odstraníme vzorový kód (`Word`, `WordsControler`), zjednodušíme `IndexControler` (žádná DB logika) a každou službu implementujeme jako samostatnou Jakon `StaticPage` – tedy třídu rozšiřující `AbstractStaticPage` s anotací `@StaticPage`, vlastní URL a Pebble šablonou.

**Jakon StaticPage API (z JAR `cz.kamenitxan.jakon.core.custom_pages`):**
- `@StaticPage` – Java anotace na úrovni třídy (retention RUNTIME)
- `AbstractStaticPage(templateName: String, pageUrl: String)` – abstraktní třída implementující `IController`
- `Director.registerCustomPage(page: AbstractCustomPage)` – registrace v `AppInit.daoSetup()`

**Tři služby:**
| Služba | URL | Šablona | Třída |
|---|---|---|---|
| Programování Java/Scala | `/sluzby/programovani` | `service-programming` | `ProgrammingPage` |
| SOHO sítě Ubiquiti | `/sluzby/site-ubiquiti` | `service-networking` | `NetworkingPage` |
| Smart Home integrace | `/sluzby/smart-home` | `service-smart-home` | `SmartHomePage` |

---

## Krok 1 – Konfigurace projektu (`jakon_config.properties`)

Upravit `modules/backend/jakon_config.properties`:
- Přidat řádek `appName=Kamenitxan.eu`
- `hostname` změnit na produkční hodnotu (např. `kamenitxan.eu`)

---

## Krok 2 – Smazání souborů `Word`

**Smazat** (vzorový kód z šablony, není potřeba):
- `modules/backend/src/main/java/cz/kamenitxan/templateapp/entity/Word.scala`
- `modules/backend/src/main/java/cz/kamenitxan/templateapp/controler/WordsControler.scala`
- `modules/backend/src/main/resources/sql/Word.sql`

---

## Krok 3 – Zjednodušení `IndexControler.scala`

Upravit `modules/backend/src/main/java/cz/kamenitxan/templateapp/controler/IndexControler.scala`:
- Odebrat mrtvý SQL `ALL_PAGES_SQL` a veškerou DB logiku
- Controller pouze vyrendruje šablonu s prázdným kontextem

```scala
def generate(): Unit = {
  val e = TemplateUtils.getEngine
  e.render(template, "index.html", Map.empty)
}
```

---

## Krok 4 – Vytvoření tříd StaticPage pro každou službu

**Vytvořit** tři nové soubory v `modules/backend/src/main/java/cz/kamenitxan/templateapp/controler/`:

```scala
// ProgrammingPage.scala
@StaticPage
class ProgrammingPage extends AbstractStaticPage("service-programming", "/sluzby/programovani")

// NetworkingPage.scala
@StaticPage
class NetworkingPage extends AbstractStaticPage("service-networking", "/sluzby/site-ubiquiti")

// SmartHomePage.scala
@StaticPage
class SmartHomePage extends AbstractStaticPage("service-smart-home", "/sluzby/smart-home")
```

Každá třída nepotřebuje žádnou další logiku – `AbstractStaticPage.generate()` automaticky vyrendruje šablonu dle `templateName` s prázdným kontextem.

---

## Krok 5 – Aktualizace `AppInit.scala`

Upravit `modules/backend/src/main/java/cz/kamenitxan/templateapp/AppInit.scala`:
- Odebrat importy a registraci `Word` a `WordsControler`
- Odebrat `DBHelper.addDao(...)` – žádné vlastní entity
- Registrovat `IndexControler` a tři `StaticPage` třídy

```scala
override def daoSetup(): Unit = {
  Director.registerController(new IndexControler)
  Director.registerCustomPage(new ProgrammingPage)
  Director.registerCustomPage(new NetworkingPage)
  Director.registerCustomPage(new SmartHomePage)
}
```

---

## Krok 6 – Přepis `core.peb` (hlavní layout)

Upravit `modules/backend/templates/templateapp/core.peb` – kompletní nový layout:
- `<html lang="cs" data-theme="dark">`
- Do `<head>`: DaisyUI CDN + Tailwind Play CDN
- Odebrat staré `reset.css`, `main.css`, `vendor.js`
- Ponechat `<script src="/js/app.js" defer>` pro Scala.js
- **Navbar**: logo/jméno vlevo, menu vpravo – položky `Domů` (`/`), `Služby` (`#sluzby`), `O mně` (`#o-mne`), `Kontakt` (`#kontakt`)
- **Footer**: jméno, rok, GitHub/LinkedIn
- `{% block content %}{% endblock %}` mezi navbar a footer

```html
<!doctype html>
<html lang="cs" data-theme="dark">
<head>
  <meta charset="utf-8">
  <title>Kamenitxan – IT služby</title>
  <link href="https://cdn.jsdelivr.net/npm/daisyui@5/dist/full.min.css" rel="stylesheet">
  <script src="https://cdn.tailwindcss.com"></script>
  <script src="/js/app.js" defer></script>
</head>
<body>
  <div class="navbar bg-base-200 shadow-md">...</div>
  {% block content %}{% endblock %}
  <footer class="footer footer-center p-4 bg-base-300 text-base-content">...</footer>
</body>
</html>
```

---

## Krok 7 – Přepis `index.peb` (hlavní stránka)

Upravit `modules/backend/templates/templateapp/index.peb` – statický obsah:

### Hero sekce
- DaisyUI `hero` s `min-h-[60vh]`, headline, podtitul, CTA tlačítko (`btn btn-primary`) na `#sluzby`

### Sekce Služby (`#sluzby`)
- Tři DaisyUI `card` komponenty v gridu (`grid grid-cols-1 md:grid-cols-3 gap-6`)
- Každá karta odkazuje na detail služby (`/sluzby/programovani` atd.)
- Ikona (SVG/emoji), název, krátký popis (1–2 věty), tlačítko „Více informací"

### Sekce O mně (`#o-mne`)
- Krátký statický text + DaisyUI `badge` tagy pro tech stack (Scala, Java, UniFi, Home Assistant, Linux, Docker...)

### Sekce Kontakt (`#kontakt`)
- DaisyUI `form-control` formulář, `action="mailto:..."`, inputy pro jméno, e-mail, zprávu

---

## Krok 8 – Vytvoření šablon pro stránky služeb

**Vytvořit** tři nové šablony v `modules/backend/templates/templateapp/`:

Každá šablona rozšiřuje `core` a obsahuje detailní popis služby:
- `service-programming.peb` – Vývoj v Java/Scala: stack, typy projektů, ukázky, cena
- `service-networking.peb` – SOHO sítě Ubiquiti: co zahrnuje instalace, hardware, podpora
- `service-smart-home.peb` – Smart Home: podporované systémy (Home Assistant, atd.), ukázky automatizací

Struktura každé šablony:
```html
{% extends "core" %}
{% block content %}
<div class="container mx-auto py-12 px-4">
  <h1 class="text-4xl font-bold mb-4">Název služby</h1>
  <!-- DaisyUI komponenty: steps, timeline, collapse pro FAQ, pricing table -->
  <a href="/#kontakt" class="btn btn-primary mt-8">Nezávazná poptávka</a>
</div>
{% endblock %}
```

---

## Krok 9 – Tailwind/DaisyUI produkční build (volitelné)

Pro produkci nahradit Play CDN skutečným buildem:
- Přidat `package.json` do `modules/frontend/` s `tailwindcss`, `daisyui` a `vite`
- Výstup CSS kopírovat do `modules/backend/src/main/resources/static/css/`
- Upravit `build.sbt` o volitelný npm task (nebo spouštět samostatně před `fullOptCompileCopy`)

---

## Další úvahy

1. **Kontaktní formulář:** `mailto:` action postačí pro MVP; pro plnohodnotné zpracování přidat `ContactControler` s POST endpointem a Jakon SMTP konfigem (`MAIL.*`).
2. **DaisyUI téma:** `data-theme="dark"` nebo `"light"` v `<html>` tagu; přepínač témat lze přidat přes Scala.js v `app.js`.
3. **Breadcrumbs na stránkách služeb:** DaisyUI `breadcrumbs` komponenta pro navigaci zpět na hlavní stránku.
4. **Rozšíření webu:** Budoucí dynamické sekce (Blog, Reference, Projekty) lze přidat jako nové Jakon entity + controllery bez zásahu do stávajícího kódu.
5. **Lokalizace:** `defaultLocale=cs_CZ` je již nastaveno; Jakon i18n klíče `{{ i18n("klíč") }}` pro případné vícejazyčné rozšíření.







