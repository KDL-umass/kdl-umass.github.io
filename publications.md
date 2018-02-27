---
layout: page
topics:
- Causal Modeling
- Statistical Relational Learning
- Navigation and Routing in Networks
- Privacy and Networks
- Fraud Detection and Security
- Citation Analysis
- Social Media Analysis
- Overfitting and Multiple Comparisons
---

{% for topic in page.topics %}
  {% assign y1 = topic %}
  <h1>{{ topic }}</h1>
  <ul>
    {% for pub in site.pubs %}
    {% assign y2 = pub.topic %}
    {% if y1 == y2 %}
    <li class="nobullet pub"><a href="{{ pub.permalink }}">{{ pub.title }}</a>&nbsp;&mdash;&nbsp;{{ pub.authors }} ({{ pub.year }}). In Proceedings of {{ pub.venue }}. [<a href="{{ pub.pdfurl }}">PDF</a>]</li>
    {% endif %}
    {% endfor %}
  </ul>
{% endfor %}
