INSERT INTO usuarios_perfis (usuario_id, perfil_id)
SELECT
    usuario.id,
    perfil.id
FROM usuarios usuario
CROSS JOIN perfis perfil
WHERE perfil.nome = 'USUARIO'
  AND NOT EXISTS (
      SELECT 1
      FROM usuarios_perfis usuario_perfil
      WHERE usuario_perfil.usuario_id = usuario.id
        AND usuario_perfil.perfil_id = perfil.id
  );
