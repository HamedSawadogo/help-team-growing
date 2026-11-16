-- Script rejouable : chaque exécution ajoute :nb nouveaux produits (100 par défaut),
-- chacun rattaché à une catégorie aléatoire et à 1..3 tags aléatoires.
-- Les catégories et tags ne sont créés que s'ils n'existent pas encore.
--
-- Usage :
--   docker exec -i catalog-db psql -U catalog -d catalog < sql/insert-products.sql
--   docker exec -i catalog-db psql -U catalog -d catalog -v nb=5000 < sql/insert-products.sql

\if :{?nb}
\else
  \set nb 100
\endif

BEGIN;

INSERT INTO category (name)
SELECT c
FROM unnest(ARRAY['Électronique', 'Maison', 'Sport', 'Mode', 'Livres', 'Jardin', 'Beauté', 'Jouets']) AS c
WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = c);

INSERT INTO tag (name)
SELECT t
FROM unnest(ARRAY['Nouveau', 'Promo', 'Bio', 'Éco-responsable', 'Best-seller', 'Made in France', 'Premium', 'Édition limitée']) AS t
WHERE NOT EXISTS (SELECT 1 FROM tag WHERE name = t);

WITH new_products AS (
    INSERT INTO product (name, description, price, category_id, created_at)
    SELECT
        (ARRAY['Casque', 'Clavier', 'Lampe', 'Chaise', 'Ballon', 'Veste', 'Roman', 'Sécateur', 'Crème', 'Puzzle'])[1 + floor(random() * 10)::int]
            || ' ' ||
        (ARRAY['Pro', 'Classic', 'Ultra', 'Mini', 'Max', 'Lite', 'Vintage', 'Smart'])[1 + floor(random() * 8)::int]
            || ' #' || substr(md5(random()::text), 1, 6),
        'Produit généré automatiquement le ' || to_char(now(), 'DD/MM/YYYY HH24:MI'),
        round((5 + random() * 995)::numeric, 2),
        -- la référence à g force une ré-évaluation par ligne (sinon même catégorie partout)
        (SELECT id FROM category WHERE g > 0 ORDER BY random() LIMIT 1),
        now() - (random() * interval '365 days')
    FROM generate_series(1, :nb) AS g
    RETURNING id
)
INSERT INTO product_tag (product_id, tag_id)
SELECT np.id, t.id
FROM new_products np
CROSS JOIN LATERAL (
    SELECT id FROM tag WHERE np.id > 0 ORDER BY random() LIMIT 1 + floor(random() * 3)::int
) t;

COMMIT;

SELECT count(*) AS total_products FROM product;
