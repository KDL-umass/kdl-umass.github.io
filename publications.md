---
layout: page
years:
 - 2018
 - 2017
 - 2016
 - 2015
 - 2014
 - 2013
 - 2012
 - 2011
 - 2010
 - 2009
 - 2008
 - 2007
 - 2006
 - 2005
 - 2004
 - 2003
 - 2002
---

{% for topic in site.topics %}
  <h1>{{ topic.text }}</h1>
  <ul>
    {% for year in page.years %}
        {% for pub in site.pubs %}
           {% if topic.key == pub.topic %}
               {% if year == pub.year %}
               <li class="nobullet pub">{% if pub.permalink %}<a href="{{ pub.permalink }}">{{ pub.title }}</a>{% else %}{{ pub.title }}{% endif %}&nbsp;&mdash;&nbsp;{{ pub.authors }} ({% if pub.shortvenue == nil %}{{ pub.longvenue }}{% else %}{{ pub.shortvenue }}{% endif %} {{ pub.year }}) [<a href="{{ pub.pdfurl }}">PDF</a>]</li>
           {% endif %}{% endif %}
       {% endfor %}
    {% endfor %}
  </ul>
{% endfor %}
