unitsize(1cm);
settings.tex = "pdflatex";

// user
draw(circle((0, 1.7), 0.7));
draw((-0.2, 1.9) -- (-0.2, 1.8));
draw((0.2, 1.9) -- (0.2, 1.8));
draw((-0.15, 1.6) .. (0, 1.5) .. (0.15, 1.6));
draw((0, 1) -- (0, -1));
draw((-1, -2) -- (0, -1) -- (1, -2));
draw((-1, 0) -- (1, 0));
label("Utente", (0, -2), down);


// action 1
draw(ellipse((8, 3), 2, 1));
label("Monitorare i treni", (8, 3), (0, 0));
draw((1, 1) -- (5.5, 2.5), Arrow);

// action 2
draw(ellipse((10, 0), 2, 1));
label("Mostrare i treni", (10, 0), up);
label("in zona", (10, 0), down);
draw((1.5, 0) -- (7.5, 0), Arrow);

// action 3
draw(ellipse((8, -3), 2, 1));
label("Cercare un treno", (8, -3), (0, 0));
draw((1, -1) -- (5.5, -2.5), Arrow);

// action 4
draw(ellipse((17, 0), 2.3, 1));
label("Visualizzare le infor-", (17, 0), up);
label("mazioni di un treno", (17, 0), down);
draw((10.5, 3){right} .. (15, 1), Arrow);
draw((12.5, 0){right} .. (14.5, 0), Arrow);
draw((10.5, -3){right} .. (15, -1), Arrow);
