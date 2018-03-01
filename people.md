---
layout: page
---

<!-- For individual bios-->
<div class="container-fluid person">
  {% assign left = true %}
  {% for member in site.people %}
  {% if member.publish and member.name %}
    {% if left %}
      <div class='row'>
    {% endif %}
    <div class="col-sm-6 col-md-6 col-lg-6">
      <img alt="{{ member.name }}"
           src="{{ member.thumbnail }}"
           class="thumbnail"/>
      <ul class="nobullet">
        <li><b><a href="{{ member.webpage }}">{{ member.name }}</a></b></li>
        <li><b>{{ member.role }}</b>
            {% if member.hastitle %}
              &nbsp;&middot;&nbsp;&nbsp;{{ member.title }}
            {% endif %}
        </li>
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
{% endif %}
{% endfor %}
</div>

<!-- For alums-->
<br>
<h1><font size="6">Alumni</font></h1>

- [Dan Corkill](https://people.cs.umass.edu/~cork/) (Senior Research Scientist, 2003-2018)
- [Hüseyin Oktay](https://www.linkedin.com/in/huseyin-oktay-715aa915/) (PhD, 2017) — Apple
- [Katerina Marazopoulou](https://www.linkedin.com/in/katerina-marazopoulou) (PhD, 2017) — Facebook
- [David Arbour](https://www.linkedin.com/in/david-arbour/) (PhD, 2017) — Facebook
- [Lisa Friedland](http://www.lazerlab.net/people/lisa-friedland) (PhD, 2016) — Northeastern University
- [Matthew Cornell](https://www.linkedin.com/in/matthewcornell/) (Research Software Architect, 1999-2007, 2011-2015) 
- [Brian Taylor](https://www.linkedin.com/in/brianjtaylor1/) (PhD, 2015) — Amazon
- [Daniel Garant](https://www.linkedin.com/in/danielgarant/) (MS, 2015) — C&S Wholesale Grocers
- [Lissa Baseman](http://lissalytics.com/) (MS, 2015) — Los Alamos National Laboratory
- [Marc Maier](https://www.linkedin.com/in/maiermarc/) (PhD, 2014) — MassMutual
- [Phillip Kirlin](http://www.cs.rhodes.edu/%7Ekirlinp/) (PhD, 2014) — Rhodes College
- [Matthew Rattigan](https://www.linkedin.com/in/mattratt/) (PhD, 2012) — University of Massachusetts Amherst
- [Andrew Fast](https://www.linkedin.com/in/andrew-fast-2a2b483/) (PhD, 2010) — CounterFlow AI
- [Michael Hay](http://www.colgate.edu/facultysearch/FacultyDirectory/michael-hay) (PhD, 2010) — Colgate University
- [Agustin Schapira](http://www.agustinschapira.com) (Senior Software Architect, 2002-2007) 
- [Amy McGovern](http://www.mcgovern-fagg.org/amy/) (Postdoc, 2002-2004) 
- [Brian Gallagher](https://people.llnl.gov/gallagher23) (MS, 2004) — Lawrence Livermore National Laboratory
- [Ross Fairgrieve](https://www.linkedin.com/in/ross-fairgrieve-b219612/) (MS, 2004) — Tumblr
- [Jennifer Neville](https://www.cs.purdue.edu/homes/neville/) (PhD, 2006) — Purdue University
