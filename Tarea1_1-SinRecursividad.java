import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

public class Tarea1_1
{
    public static void main( String[] args )
    {
        
        DateTimeFormatter fechaFormateada=DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        //Iniciamos un DateTimeFormatter previamente para su futuro uso en el programa, añadiendo el formato de 
        // dia, mes, año, horas y segundos de la forma en la que se indica.

        int maxVisibleFiles=(args.length > 0)? Integer.parseInt(args[0]) : 0;

        //Pedimos al usuario que inserte (o no) la cantidad maxima de ficheros o directorios que 
        // seran visibles en la proxima busqueda.
        

        Path directorioPersonal=Path.of(System.getProperty("user.home")); 

        //El path que usaremos para ejecutar el programa en todo momento es nuestro directorio personal.

        if(!Files.exists(directorioPersonal)){
            System.out.println("El directorio o fichero "+directorioPersonal+" no existe");   
            return;
        }

        //Comprobamos nuestro directorio personal, viendo si existe o no, en caso de no existir enviamos un 
        // mensaje y hacemos un return,

        Stream<Path> ficheros=null;

        //Cramos una variable de tipo Stream<Path> donde guardaremos los diferentes archivos o directorios que 
        //encontremos en con el programa.
                
        try{

            ficheros = Files.list(directorioPersonal);

            //Listamos en el stream los ficheros o directorios situados en la ruta del usuario. 

            if(maxVisibleFiles > 0){
                ficheros = ficheros.limit(maxVisibleFiles);
            }

            //Comprobamos si la variable maxVisibleFiles tiene un dato numerico el cual indique el limite de 
            //comprobacion de ficheros de nuestro programa, en caso contrario este if es omitido.

            ficheros.forEach(f -> {

            //Iniciamos el foreach con el cual visualizaremos los ficheros de nuestro equipo

                try{

                    //Condicion de control para evitar que el programa lea cualquier fichero oculto
                    //o cualquier link simbolico.

                    if(Files.isHidden(f) || Files.isSymbolicLink(f)){
                        return;
                    }

                    System.out.print(f.getFileName());
            
                    //Indicamos el nombre de un fichero con un f.getFileName.

                    if(Files.isDirectory(f)){
                        System.out.print("/");
                    }
            
                    //En caso de ser un directorio, le añadimos una barra final para destacarlo como uno.

                    if(Files.isRegularFile(f)){

                        FileTime ultimaModificacion = Files.getLastModifiedTime(f);
                        LocalDateTime fecha = LocalDateTime.ofInstant(ultimaModificacion.toInstant(), ZoneId.systemDefault());

                        //Usamos FileTime para saber en segundos cuando fue la ultima modificacion del fichero.
                        //Tambien usamos LocalDateTime junto a la variable ultimaModificacion para que se muestre 
                        //de tal forma que la hora y fecha sea segun la zona horaria en la que nos situamos.

                        System.out.print(" | " + fecha.format(fechaFormateada));

                        //Imprimimos la fecha usando ahora si el DateTimeFormat que preparamos previamente en el programa

                        System.out.print(" | " + Files.getOwner(f).getName());

                        //Imprimimos el nombre del propietario del fichero.

                        System.out.print(" | " + String.format("%.2f", Files.size(f) / 1024.0) + " KiB");

                        //Imprimimos en formato KiB el tamaño del fichero quee stamos viendo
                    }

                    //If para saber si lo que estamos observando es un archivo y no un fichero.

                }
                catch(IOException e){
                    System.out.print(" | Error al obtener los datos del fichero: " + e.getMessage());
                }

            //Try-Catch para asegurarnos de que en caso de salir un error sea mencionado por pantalla

                System.out.println();
            
            //Salto de linea para evitar que todo se inserte en una sola linea.

            });
        }
        catch(IOException | UncheckedIOException e){
            System.out.println("No se pudo acceder a " + directorioPersonal + ": " + e.getMessage());
        }

        //Catch que ejecuta una IOException o una UncheckedIOException en caso de que suceda un error

        finally{
            if(ficheros != null){
                ficheros.close();
            }
        }

        //Finally extra que usamos para cerrar el Stream de ficheros.
    }
}