# Frontend

SPA em Angular 22 para o `vant.orders`. A documentação completa (API, worker, Docker e CI) está no [README da raiz](../README.md).

```bash
npm ci
npm start          # http://localhost:4200
npm test           # testes do componente raiz
npm run build      # build de produção em dist/frontend/browser
```

A API é resolvida em `src/app/orders.service.ts`: usa `http://localhost:8080` em desenvolvimento, a própria origem em produção e cai para o modo demonstração quando não há resposta.
