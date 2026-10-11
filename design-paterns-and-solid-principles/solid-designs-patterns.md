## SOLID PRINCIPLES

S => Single Responsibility
une classe doit avoir un seul niveau de responsabilité ex: si j'ai sendEmail dans userService la question est estceque c'est a UserService de garantir l'envoie d'email ?
ou esceque sendEmail  pourrai  etre utilisé sans connaitre le deps de USersvice ? si oui alors déplacer car c'est pas sa responsabilité
l'idée c'est de rendre les classes en composant qui s'il ya un soucis est a eux qu'on penses a 100% en mm temps car c'est eux qui ont la responsabilité de ce service
ce qui augmente la cohesion et réduis le couplage et augmenter la testabilité

ça veux pas dire une classe par méthode non!, ça veut dire que toutes les méthodes de la classe doivent répondre a une problematique tres precise et toues les méthodes, atrribus,.. doivent etre crées pour ce mm but.

O => Open closed

un code doit etre fermé a la modif et ouvert a l'extension ça veut pas dire quoi doit pas modifier le code mais plutot, moins le modifier pour ajouter certains comportements
l'idée c'est d'utiiser des abstractions que sorte que l'app (les service soit couplé aux abstractions et non aux détails d'impl)

L => Liskov subtitution
Attentiion a l'héritage, la question a ce poser c'est le IllegalArgument..() est justifié ou pas. si ou bonne impl sinon a revoir

I => Interface segregation
eviter les interfaces trop generiques qui renvoit au pricipe L

D => Dependency Inversion
(les classe de haut niveu ne doivent pas etre  couplé aux détails plutot aux abstractions et non aux détails d'impl)


## Designs Patterns


### Strategy

generalemt quand on a des

if (type == A)
...

else if (type == B)

...

et quon sait que type peut évoluer dans lapp (c'est a dire on peut avoir C,D,...) generalement c'est dangeureux car le soucis c'est que ce genre de code on va le retrouver

partout dans l'app. et si j'oublie une partie BOOOOM . l'idée c'est de metre les comportement des des classes par type et avoir un seul endroit a modifier en cas d'ajout et créer une classe spécifique pour ce nouveau type. car c'est difiicile de rechercher des blocs de code dans un logciel mais facile de rechercher des classes.


### Factory

Factory c'est une classe de fabrique b'objet , ça peut etre interessant si j'ai plusieurs classes a créer par type donnée. toujours pour éviter ce que j'ai mentionné dans le pattern stratégie ce code serra transporté partout