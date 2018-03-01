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
- 2001
- 200
---

<!-- For individual bios-->
<div class="container-fluid person">
  {% assign left = true %}
  {% for member in site.people %}
  {% if member.publish and member.name %}
  {% unless member.alum %}
    {% if left %}
      <div class='row'>
    {% endif %}
    <div class="col-sm-6 col-md-6 col-lg-6" style="margin-bottom:25px">
      <img alt="{{ member.name }}"
           src="{{ member.thumbnail }}"
           class="thumbnail col-sm-12 col-xs-12"/>
      <ul class="nobullet">
        <li><b><a href="{{ member.webpage }}">{{ member.name }}</a></b></li>
        <li><b>{{ member.role }}</b></li>
        {% if member.hastitle %}
           <li>{{ member.title }}</li>
        {% endif %}
 	{% if member.email %}
        <li><span class="glyphicon glyphicon-envelope" aria-hidden="true"></span>&nbsp;{{ member.email }}</li>
	{% endif %}
      </ul>
      <p> {{ member.content }}</p>
    </div>
{% if left %}
  {% assign left = false %}
{% else %}
  </div>
  {% assign left = true %}
{% endif %}
{% endunless %}{% endif %}
{% endfor %}
</div>

<!-- For alums-->
<br>
<h1 align="center"><font size="6">Alumni</font></h1>
<div class="row">
<div class="col-xs-12 col-sm-12 col-md-6 col-lg-6 col-xl-6">
<ul class="nobullet">
{% assign numleftcol = 0 %}
{% assign onleftcol = true %}
{% assign pad = true %}
{% for year in page.years %}
   {% assign numpeople = site.people | size %}
   {% assign leftcolsize = numpeople | divided_by: 2 %}
       {% for person in site.people %}
         {% if numleftcol >= leftcolsize and onleftcol %}
           </ul></div>
           <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6 col-xl-6">
           <ul class="nobullet">
           {% assign onleftcol = false %}
         {% endif %}
         {% assign temp = numleftcol | plus: 1 %}
         {% if person.alum and person.endyear == year %}
           {% assign numleftcol = temp %}
           <li><a href="{{ person.webpage }}">{{ person.name }}</a>
             ({% if person.role %}{{ person.role }}{% else %}{{ person.degree }}{% endif %},
              {% if person.startyear %}{{ person.startyear }}-{% endif %}{{ person.endyear }}) - {{ person.current }}
           </li>
         {% endif %}
         {% unless person.alum %}
           {% if pad %}
             {% assign numleftcol = temp %}
             {% assign pad = false %}
           {% endif %}
         {% endunless %}
     {% endfor %}
{% endfor %}
</ul></div></div>
