import { createApp } from "./app.mjs";

const { app } = createApp();
app.listen(3000, "127.0.0.1", () => {
  console.log("Express lesson server listening on http://127.0.0.1:3000");
});
