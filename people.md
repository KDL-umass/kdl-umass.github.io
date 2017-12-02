---
layout: page
---
<!-- http://stackoverflow.com/questions/2245972/modulus-or-lack-thereof-in-rubys-liquid-templating-engine -->

<div class="container-fluid person">
  {% for member in site.people %}
  {% if member.publish and member.name %}
  {% cycle 'add row' : '<div class="row">', nil %}
    <div class="col-md-6">
      <img alt="{{ member.name }}"
           src="{{ member.thumbnail }}"
           class="thumbnail"/>
      <ul class="nobullet">
        <li><b><a href="{{ member.webpage }}">{{ member.name }}</a></b></li>
        <li><b>{{ member.title }}</b></li>
	{% if member.email %}
        <li><span class="glyphicon glyphicon-envelope" aria-hidden="true"></span>&nbsp;{{ member.email }}</li>
	{% endif %}
      </ul>
      <p> {{ member.content }}</p>
    </div>
{% endif %}
{% endfor %}
{% cycle 'close rows': nil, '</div>', '</div>' %}
</div>
