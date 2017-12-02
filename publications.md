---
layout: page
years:
- 2017
- 2016
- 2015
---

{% for year in page.years %}
  {% assign y1 = year %}
  <h1>{{ year }}</h1>
  <ul>
    {% for pub in site.pubs %}
    {% assign y2 = pub.year %}
    {% if y1 == y2 %}
    <li class="nobullet pub"><a href="{{ pub.permalink }}">{{ pub.title }}</a>&nbsp;&mdash;&nbsp;{{ pub.authors }} ({{ pub.year }}). In Proceedings of {{ pub.venue }}. [<a href="{{ pub.pdfurl }}">PDF</a>]</li>
    {% endif %}
    {% endfor %}
  </ul>
{% endfor %}
