package com.spookyx;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import org.apache.commons.cli.*;
import com.opencsv.CSVWriter;


public class EEVectROM 
{
    private static final byte QUIT_APP  = 1;
    private static final byte NO_ERROR  = 0;

    //  Verify if file is a vectrex rom with first 2 magic bytes
    public static boolean verifyVectrexFile( File fileName )
    {
        boolean result  = false;
        byte    magic[] = new byte[5];

        try  
        {
            FileInputStream fis_magic = new FileInputStream( fileName );

            fis_magic.read( magic );

            result = (  magic[0] == 0x67 && magic[1] == 0x20 && 
                        magic[2] == 0x47 && magic[3] == 0x43 && 
                        magic[4] == 0x45 ) ? true : false;
            
            fis_magic.close();
        }
        catch( Exception e )  
        {  
            e.printStackTrace();
        }  
        
        return result;
    }

    public static void writeBytes( FileOutputStream writeStream, long nBytes, int writeBytes )
    {
        for ( long bytesCount = 0x0000; bytesCount < nBytes; bytesCount++ )
        {
            try
            {
                writeStream.write( writeBytes );
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
    }

    public static void writeBlankBytes( FileOutputStream writeStream, long nBytes, int writeBytes )
    {
        for ( long bytesBlankCount = 0x0000; bytesBlankCount < nBytes; bytesBlankCount++ )
        {
            try
            {
                writeStream.write( writeBytes );
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
    }

    public static void writeBytesFromRead( FileInputStream readStream, FileOutputStream writeStream, long nBytes )
    {
        for ( long bytesBlankCount = 0x0000; bytesBlankCount < nBytes; bytesBlankCount++ )
        {
            try
            {
                writeStream.write( readStream.read() );
            }
            catch ( IOException e )
            {
                e.printStackTrace();
            }
        }
    }

    public static byte writeROMFile( String folder, ArrayList<String> vectrexFileList, ArrayList<Integer> bytesBankList, boolean createCVS, boolean createEEP )
    {
        int countVecFile    = 0;
        int count16KiloROM  = 0;
        //int count8KiloROM   = 0;

        //File fileROM = new File( folder + "/Export/" + "VectROM.ROM" );

        File fileROM = new File( folder + "/Export/" );
        fileROM.mkdir();

        fileROM = new File( folder + "/Export/VectROM.ROM" );

        Scanner sc      = new Scanner( System.in );
        String  input   = new String();

        try
        {
            countVecFile = vectrexFileList.size();

            if ( !fileROM.createNewFile() )
            {
                System.out.println( "Info : Vectrex ROM already exist." );

                System.out.print( "Do you want overwrite ROM ( Y : to overwrite - Q for Quit ) ? " );

                ////Scanner sc = new Scanner( System.in );
                input = sc.nextLine();
                    
                if ( input.contains( "Q" ) || input.contains( "q" ) )
                {
                    sc.close();

                    return QUIT_APP;
                }
            }

            FileOutputStream writeStream = new FileOutputStream( fileROM );

            for ( int count = 0; count < countVecFile; count++ )
            {
                File vectrexFile = new File( folder + "/" + vectrexFileList.get( count ) );

                long vectrexFileLength = vectrexFile.length();

                FileInputStream readStream = new FileInputStream( vectrexFile );

                int bytesBank = 0x00;

                //  4 Kilos Vectrex ROM
                if ( vectrexFileLength == 0x1000 )
                {
                    writeBytesFromRead( readStream, writeStream, vectrexFileLength );

                    writeBlankBytes( writeStream, ( 0x2000 - vectrexFileLength ), 0x00 );

                    bytesBank = count * 0x04 + 0x03;
                }
                //  4-8 Kilos Vectrex ROM
                else if ( vectrexFileLength > 0x1000 && vectrexFileLength <= 0x2000 )
                {
                    writeBytesFromRead( readStream, writeStream, vectrexFileLength );

                    if ( vectrexFileLength < 0x2000 )
                    {
                        writeBlankBytes( writeStream, ( 0x2000 - vectrexFileLength ), 0x00 );
                    }

                    //count8KiloROM++;

                    bytesBank = count * 0x04 + 0x03;
                }
                //  8-16 Kilos Vectrex ROM
                else if ( vectrexFileLength > 0x2000 && vectrexFileLength <= 0x4000 )
                {
                    writeBytesFromRead( readStream, writeStream, vectrexFileLength );

                    if ( vectrexFileLength < 0x4000 )
                    {
                        writeBlankBytes( writeStream, ( 0x4000 - vectrexFileLength ), 0x00 );
                    }

                    bytesBank = ( count + count16KiloROM ) * 0x04 + 0x00;
                    //bytesBank = ( count + count16KiloROM ) * 0x04 + 0x03;

                    count16KiloROM++;
                }
                
                readStream.close();   

                bytesBankList.add( bytesBank );

            }
            writeStream.close();

            System.out.println( "\nVectrex ROM create Success...\n" );

            //  Create CVS File
            if ( createCVS == true )
            {
                File fileCVS = new File( folder + "/Export/VectrexRomList.cvs" );

                ////Scanner sc2 = new Scanner( System.in );
                if ( fileCVS.exists() )
                {
                    System.out.println( "Info : Vectrex ROM List already exist." );
                    System.out.print( "Do you want overwrite List ( Y : to overwrite - Q for Quit ) ? " );

                    input = sc.nextLine();
                    
                    if ( input.contains( "Q" ) || input.contains( "q" ) )
                    {
                        sc.close();

                        return QUIT_APP;
                    }
                    else if ( input.contains( "Y" ) || input.contains( "y" ) )
                    {
                        createCVS( folder, vectrexFileList, bytesBankList );

                        System.out.println( "\nCVS file create Success..." );
                    }
                }
                else
                {
                    createCVS( folder, vectrexFileList, bytesBankList );

                    System.out.println( "\nCVS file create Success..." );
                }
            }

            //  Create EEP File
            if ( createEEP == true )
            {
                File byteBanksEEP = new File( folder + "/Export/byteBanksEEP.eep" );

                //////Scanner sc3 = new Scanner( System.in );
                if ( byteBanksEEP.exists() )
                {
                    System.out.println( "Info : EEP EEPROM file already exist." );
                    System.out.print( "Do you want overwrite EEPROM file ( Y : to overwrite - Q for Quit ) ? " );

                    input = sc.nextLine();
                    
                    if ( input.contains( "Q" ) || input.contains( "q" ) )
                    {
                        sc.close();

                        return QUIT_APP;
                    }
                    else if ( input.contains( "Y" ) || input.contains( "y" ) )
                    {
                        createByteBanksEEP( byteBanksEEP,  bytesBankList );
                    }
                }
                else
                {
                    createByteBanksEEP( byteBanksEEP, bytesBankList );
                }

                System.out.println();
                System.out.println( "Create EEP File.");
                System.out.println();
            }

            sc.close();

        }           
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        
        return 0;
    }

    public static void createCVS( String strDir, ArrayList<String> romListSize, ArrayList<Integer> bytesBankList )
    {      
        CSVWriter csvWriter;

        try
        {
            csvWriter = new CSVWriter( new FileWriter( strDir + "/Export/VectrexRomList.cvs" ) );
                                
            csvWriter.writeNext( new String[]
            {
                "No", "Vectrex ROM Name", "File Size", "Vectrex ROM Mapping"
            } );

            for ( int count = 0; count < romListSize.size(); count++ )
            {
                File vecFile = new File( strDir + "\\" + romListSize.get( count) );

                long mappingROM = ( bytesBankList.get( count ) >> 0x02 ) << 0x0D;

                csvWriter.writeNext( new String[]
                {
                    "No. " + Integer.toString( count + 1 ), 
                    romListSize.get( count ), 
                    vecFile.length() + " Bytes",
                    convertDec2Hex( mappingROM, true )
                } );
            }
    
            csvWriter.close();

            System.out.println();
            System.out.println( "Create Vectrex List CVS File.");
            System.out.println();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

    }

    //  TODO : Create EEP byte Banks : Input start bansks vectrex game ( line 307 )
    public static void createByteBanksEEP( File byteBanksEEP, ArrayList<Integer> bytesBankList )
    {
        Scanner sc = new Scanner( System.in );
        System.out.print( "Enter Witch ROM Game on Vectrex Startup ( eg : 1.." +  bytesBankList.size() + " ) ? " );
        int Num = Integer.parseInt( sc.nextLine() );
        sc.close();

        if ( Num < 1 || Num > bytesBankList.size() )
        {
            System.out.println( "Wromg Value - Set to default 1." );

            Num = 0x01;
        }

        try
        {
            FileOutputStream writeStream = new FileOutputStream( byteBanksEEP );

            int countBytesBankList = bytesBankList.size();

            writeBytes( writeStream, 0x02, 0xFF );
            writeBytes( writeStream, 0x01, Num );
            writeBytes( writeStream, 0x01, countBytesBankList );            
            writeBytes( writeStream, 0x0C, 0xFF );

            for ( byte count = 0x00; count < countBytesBankList; count++ )
            {
                writeStream.write( bytesBankList.get( count ) );
            }
            
            writeBytes( writeStream, 0x200 - ( countBytesBankList + 0x10 ), 0xFF );
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }   
    }

    public static String setUserVectFolder()
    {
        Scanner sc = new Scanner( System.in );
        System.out.print( "Enter Vectrex files ( *.vec ) Folder - Enter for current Folder : " );

        String strRomDir = sc.next();
                                
        if ( strRomDir.length() == 0 )
        {
            strRomDir = FileSystems.getDefault().getPath( "." ).toString();
        }
        
        File dirVecFileFolder = new File( strRomDir );
        if ( !dirVecFileFolder.exists() )
        {
            System.out.println( "The Vectrex files Folder " + strRomDir + " doesn't exist - QUIT !!! " );
            sc.close();
            
            return "";
        }
        sc.close();

        return strRomDir;
    }

    public static String GenerateByteBanks( ArrayList<Integer> bytesBankList )
    {
        String strList = new String();

        for ( int i = 0; i < bytesBankList.size(); i++ )
        {
            strList += convertDec2Hex( bytesBankList.get( i ), false );
            strList += ( i < ( bytesBankList.size() - 1 ) ) ? ", " : " ";
        }

        return strList;
    }

    //////////////////////////
    //  Helper Function    //
    ////////////////////////
    //  Convert decimal to Hexa
    public static String convertDec2Hex( long dec, boolean wordFormat )
    {
        String hex = new String( "0x" );

        hex += ( dec <= 0x0F ) ? "0" : "";
        hex += ( dec <= 0x0F && wordFormat ) ? "00" : "";
        hex += Long.toHexString( dec ).toUpperCase();

        return hex;
    }


    public static void main( String[] args ) throws IOException
    {
        boolean createCVS   = false;
        boolean byteBanks   = false;
        boolean createEEP   = false;
        
        File dirVecFileFolder = new File( "" );      //  Fake File Initialisation  
        
        System.out.println( "Create Vectrex ROM" );
        System.out.println( "==================" );
        System.out.println();

        Options options = new Options();
        Option helpCLI = new Option( "h", "help", false, "Help for App" );
        //helpCLI.setRequired( true );
        options.addOption( helpCLI );

        Option cvsCLI = new Option( "cvs", "-createCVS", false, "Create CVS" );
        //cvsCLI.setRequired( true );
        options.addOption( cvsCLI );

        Option banksEEP = new Option( "eep", "-createEEP", false, "Generate Byte Banks EEP file" );
        //banksCLI.setRequired( true );
        options.addOption( banksEEP );

        Option banksCLI = new Option( "banks", "-createBanks", false, "Generate Byte Banks" );
        //banksCLI.setRequired( true );
        options.addOption( banksCLI );

        Option folderCLI = new Option( "f", "-setFolder", true, "Set Folder" );
        //banksCLI.setRequired( true );
        options.addOption( folderCLI );

        //Create a parser
        CommandLineParser parser = new DefaultParser();

        //parse the options passed as command line arguments
        try
        {
            CommandLine cmd;

            cmd = parser.parse( options, args );

            createCVS = ( cmd.hasOption( "cvs" ) || cmd.hasOption( "-createCVS" ) ) ? true : false;

            byteBanks = ( cmd.hasOption( "banks" ) || cmd.hasOption( "-createBanks" ) ) ? true : false;

            createEEP = ( cmd.hasOption( "eep" ) || cmd.hasOption( "-createEEP" ) ) ? true : false;

            //hasOptions checks if option is present or not
            if ( cmd.hasOption( "h") || cmd.hasOption( "-help" ) )
            {
                System.out.println( "HELP :" );
                System.out.println();
                System.out.println( "Usage : java -jar EEVectRom.jar -h --help -cvs --createCVS [Vectrex File Directory]" );
                System.out.println( "-h or -help : Help for this App." );
                System.out.println( "-cvs or --createCVS : create cvs file for excel sheet." );
                System.out.println( "-eep or --createEEP : create eep file for byte Banks EEPROM." );
                System.out.println( "-byteBanks or --createByteBanks : create byte banks file for EEVECT EEPROM." );
                System.out.println();
                System.out.println( "END Of HELP. QUIT..." );
                        
                return;
            }

            String folder = new String();

            if( cmd.hasOption( "f" ) )
            {
                folder = cmd.getOptionValue( "f" );
            }
            else if ( cmd.hasOption( "-setFolder" ) )
            {
                folder = cmd.getOptionValue( "-setFolder" );
            }    

            System.out.println( "set Folder Path : " + folder );

            dirVecFileFolder = new File( folder );

            if ( dirVecFileFolder.isDirectory() && dirVecFileFolder.exists() )
            {
                dirVecFileFolder = new File( folder );

                if ( !dirVecFileFolder.exists() || dirVecFileFolder.isFile() 
                                                || !dirVecFileFolder.isDirectory() 
                                                || dirVecFileFolder.length() == 0 )
                {
                    dirVecFileFolder = new File( setUserVectFolder() );
                }
            }
  
        }
        catch ( ParseException e1 )
        {
            e1.printStackTrace();
        }

        if ( args.length == 0 )
        {
            dirVecFileFolder = new File( setUserVectFolder() );
        }

        ArrayList<String> romList = new ArrayList<String>();

        // Search Vectrex ROM File in specified Folder
        File[] matchingFiles = dirVecFileFolder.listFiles( new FilenameFilter()
        {
            public boolean accept( File dir, String name )
            {

                return name.endsWith( "vec" );      // name.startsWith( "temp" ) &&
            }
        } );
        
        System.out.println( "\nVectrex Files List ( sort by size ) :" );
        
        Arrays.sort( matchingFiles, ( f1, f2 ) -> 
        {
            return new Long( f1.length() ).compareTo( new Long( f2.length() ) );
        } );

        for ( File file : matchingFiles )
        {
            long    sizeVectFile = file.length();
            String  nameVectFile = file.getName();

            if ( verifyVectrexFile( file ) )
            {
                romList.add( nameVectFile );
            }

            System.out.println( " File No " + romList.size() + ".\t" + nameVectFile + "\t\t- Size : " + sizeVectFile + " bytes" + "\t- Ranges : " );
        }
        System.out.println();
        
        //  Create Vectrex ROM EEPROM File
        ArrayList<Integer> bytesBankList = new ArrayList<>();

        byte result = writeROMFile( dirVecFileFolder.getPath(), romList, bytesBankList, createCVS, createEEP );
        if ( result == NO_ERROR )
        {
            System.out.println();
            System.out.println( "\nAll Process Success Done..." );
        }
        else if ( result == QUIT_APP )
        {
            System.out.println( "\nQuit Application..." );   
        }
        else
        {
            System.out.println( "\nProcess Failed..." );  
        }

        //  Genarate Byte Banks for Vectrex ROM EEPROM 
        if ( byteBanks == true )
        {
            System.out.println( "\nEEPROM config :" );
            System.out.print( "RAW BYTES : " + GenerateByteBanks( bytesBankList ) );
            System.out.println();
            System.out.println( "\nEEVECT Arduino Array : static const uint8_t Addressing[] PROGMEM = { " + GenerateByteBanks( bytesBankList ) + "};" );
            System.out.println();
        }

    }

}