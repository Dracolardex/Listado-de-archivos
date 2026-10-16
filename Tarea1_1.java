import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

public class Tarea1_1
{

    //Creamos un metodo recursivo que sirva exclusivamente para sumar el tamaño de los documentos
    //Situados dentro del directorio en el que estemos.

    public static long recorrerDirectorio(Path dir, int profundidad)

    //El metodo devuelve un dato tipo long y pide dos datos de entrada.
    //Un path que sera el directorio base del usuario.
    //un int que define la cantidad de directorios que ba a bajar el programa antes de detener la recursividad.

    {
        long suma = 0;

        //Definimos la variable suma

        DirectoryStream<Path> stream;

        //Creamos sin instanciar un DirectoryStream, este directory stream revisara 1 por 1 cada archivo
        //Que se almacene en el array de stream, no agarra toda la lista de golpe, los va tomando 1 a 1.

        try{

             stream = Files.newDirectoryStream(dir);

             //Instanciamos el directory stream

            
            for(Path p : stream){

                //Foreach para revisar el stream

                if(Files.isRegularFile(p) && !Files.isSymbolicLink(p) || !Files.isHidden(p)){
                    suma += Files.size(p);
                }

                //Condicional para saber si es un fichero normal lo que estamos leyendo ignorando enlaces simbolicos
                //Tambien ignora archivos ocultos.
                //Finalmente suma de forma continua el tamaño de los archivos

                else if(Files.isDirectory(p) && profundidad > 0 && !Files.isSymbolicLink(p) || !Files.isHidden(p)){
                    suma += recorrerDirectorio(p, profundidad - 1);
                }

                //Else if donde, en caso de ser un directorio y tener una profundidad mayor a 0, se lanza nuevamente
                //El metodo recorrerDirectorios de forma recursiva, restandole un -1 a la profundidad para ir acotando
                //La cantidad de directorios que se deben revisar.
                //Tambien nos aseguramos de que los directorios revisados no esten ocultos o no sean symbolicslinks

            }

            stream.close();

            //Cerramos el stream cuando no haya nada más que revisar.
        }
        catch(AccessDeniedException e){
        System.err.println("[sin permisos] " + dir);
        }

        //Excepcion para cuando no podamos acceder a un directorio que necesite permisos de acceso.

        catch(IOException e){
            System.err.println("[sin permisos] " + dir);
        }

        //Excepcion general por si no encontramos un fichero, archivo o si no podemos acceder a uno.

        return suma;

        //Devolvemos el resultado de la suma.
    }

    //Creamos un segundo metodo recursivo, este nos servira para imprimir de forma correcta todos los contenidos
    //Pedidos por el usuario, siendo coherentes con la cantidad de niveles que hay que bajar y la cantidad de
    //Ficheros que hay que mostrar por directorio.

    public static void mostrar(Path dir, int nivel, int maxNivel, int maxVisible,
                           DateTimeFormatter fmt, int maxProf){
    
    //El metodo pide como parametros de entrada lo siguiente:

    //Dir: Directorio en donde nos encontramos
    //nivel: NO CONFUNDIR CON EL NIVEL DE PROFUNDIDAD QUE PIDE EL USUARIO, es un nivel orientativo e indica el nivel 0
    //osea el nivel donde nos encontramos.
    //maxNivel: Cantidad maxima de niveles que bajaremos en este programa.
    //maxVisible: Cantidad maxima de ficheros que veremos en el programa.
    //fmt: variable de tipo datetimeformatter necesaria para imprimir informacion sobre la fecha de modificacion de un fichero.
    //maxProf: parametro auxiliar para el metodo de recorrerDirectorios con el fin de hacer el calculo de tamaño
    //de los directorios.

        List<Path> hijos;
    
        //Creamos una variable hijo de tipo list que usaremos más adelante.

        try(Stream<Path> s = Files.list(dir)){
        
        //Try-with-resource: es un try que tiene integrado una variable s de tipo stram, esta vairable lista los directorios
        //que usaremos más adelante, se hace asi para que cuando termine el try se cierre el stream.

            Stream<Path> visibles = s.filter(p -> {
                try{
                    return !Files.isHidden(p) && !Files.isSymbolicLink(p);
                }
                catch(IOException e){
                    return false;
                }
            });

        //Varianle visible de tipo stream a la cual, le hemos añadido un lambda donde s usa la propiedad filtrer.
        //Esta pequeña seccion con lambda nos permite hacer que en caso de encontrar un archivo oculto o un enlace simbolico
        //Podamos ignorarlo y lanzar un return para que estos archivos no sean leidos, lambda es una funcion integrada
        //Por ende el return vuelve de nuevo a comprobar si hay archivos hidden o symboliclinks.

            if(maxVisible > 0){
                visibles = visibles.limit(maxVisible);
            }
        
        //Se le añade el limite al stream visibles, siempre despues de comprobar que no hay archivos ocultos o enlaces
        //Solo queremos mostrar los archivos que si estan permitidos con cierto limite, el resto los ignoramos.

            hijos = visibles.toList();

        //Insertamos la lista de archivos de visible en hijos para que el programa pueda iniciar con normalidad.

        }
        catch(IOException | UncheckedIOException e){
            System.err.println("[sin permisos] " + dir);
            return;
        }

        //Excepcion en caso de que los directorios no puedan encontrarse a la hora de la revision
        //Unchecked hace lo mismo que IO pero permitiendo revisar lambdas o streams en caso de que estos fallen.

        String sangria = "  ".repeat(nivel);

        //Sangria como decorado para diferenciar los niveles a los que se encuentran los archivos o directorios.
        //Hacerlo de esta forma hara que cada vez que avancemos en un directorio se añada un espacio más de sangria.


        //Comenzamos a imprimir los diferentes ficheros o directorios encontrados con un foreach con lambda incluido.

        hijos.forEach(p -> {

            boolean esDir = Files.isDirectory(p);

            //Boolean simple para saber si un fichero es un directorio o no.

            String barra = "";

            if(esDir){
                barra = "/";
            }

            //Si es un fichero no se le añade nada, si es un directorio se le coloca delante una barra.

            try{
                LocalDateTime fecha = LocalDateTime.ofInstant(
                    Files.getLastModifiedTime(p).toInstant(), ZoneId.systemDefault());
                
                //Forma más resumida de darle formato a la hora y dia en la cual un fichero a sido modificado.
                //Anteriormente File.getLastModifiedTime se sacaba en una variable externa.
                //Ahora la inclumos en el propio comando para ahorrar lineas.

                long tam;
                if(esDir){
                    tam = recorrerDirectorio(p, maxProf);
                }
                else{
                    tam = Files.size(p);
                }

                //Aqui finalmente aplicamos la funcion "recorrerDirectorio"
                //Con ella sumamos el tamaño del directorio o directorios a los que accedamos.
                //En caso de no situarnos sobre un directorio y estar en un fichero solo sacamos su tamaño.
                //Todo se guarda en la variable de tipo long tam.

                System.out.println(sangria + p.getFileName() + barra
                    + " | " + fecha.format(fmt)
                    + " | " + Files.getOwner(p).getName()
                    + " | " + String.format("%.2f", tam / 1024.0) + " KiB");
                
                //En un solo println imprimimos la sangria, el nombre del fichero o directorio, la variable barra
                //la ultima fecha de modificacion usando el parametro fmt.
                //el usuario owner del archivo.
                //y el tamaño del fichero o directorio.

                //Al hacerlo en una sentencia nos evitamos espaciados inesperados.

            }
            catch(IOException e){
                System.out.println(sangria + p.getFileName() + barra + " | Error: " + e.getMessage());
            }

            //Mismo catch aunque este es un erro directamente que muestra un fallo a la hora de acceder a un directorio
            //o a la hora de leer un fichero.

            if(esDir && nivel < maxNivel){
                mostrar(p, nivel + 1, maxNivel, maxVisible, fmt, maxProf);
            }

            //Llamada recursiva con la cual, incrementaremos el nivel en el que nos situamos hasta que sea igual a maxNivel.
            //Esto solo sucede si se detecta otro directorio por explorar en la funcion.

        });
    }

    public static void main( String[] args )
    {
        
        DateTimeFormatter fechaFormateada=DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        //Iniciamos un DateTimeFormatter previamente para su futuro uso en el programa, añadiendo el formato de 
        // dia, mes, año, horas y segundos de la forma en la que se indica.

        int maxVisibleFiles=(args.length > 0)? Integer.parseInt(args[0]) : 0;

        //Pedimos al usuario que inserte (o no) la cantidad maxima de ficheros o directorios que 
        // seran visibles en la proxima busqueda.

        int maxProfundidad=(args.length > 1)? Integer.parseInt(args[1]): 0;

        //Variable que le pide al usuario como segundo parametro la profundidad maxima de directorios que quiere ver.
        
        Path directorioPersonal=Path.of(System.getProperty("user.home")); 

        //El path que usaremos para ejecutar el programa en todo momento es nuestro directorio personal.

        if(!Files.exists(directorioPersonal)){
            System.out.println("El directorio o fichero "+directorioPersonal+" no existe");   
            return;
        }

        //Una condicional para asegurarnos de que el path que tenemos existe.

        mostrar(directorioPersonal, 0, maxProfundidad, maxVisibleFiles, fechaFormateada, maxProfundidad);

        //Uso del metodo recursivo con sus implicaciones.


    }
        
}