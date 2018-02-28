---
layout: page
---
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
