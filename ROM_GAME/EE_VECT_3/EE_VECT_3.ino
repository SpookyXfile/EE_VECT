




#include <EEPROM.h>


//	Atmega328PB Arduino Pin Translation 
#define DW_BUT_PIN    23
#define UP_BUT_PIN    4   // ( 4 )
#define OK_BUT_PIN    3   // ( 3 )

#define SDATA_PIN     5
#define LATCH_PIN     6
#define CLOCK_PIN     7
#define SEG_EN0_PIN   8   // ( PB0 )
#define SEG_EN1_PIN   9   // ( PB1 )


#define SW_ADDR_1_PIN		PIN_PC1   // ( 15 )
#define SW_ADDR_2_PIN		PIN_PC2   // ( 16 )
//#define SW_ADDR_A12_PIN		PIN_P**   //
#define SW_ADDR_A13_PIN		PIN_PE2   // ( 22 )
#define SW_ADDR_A14_PIN		PIN_PC0   // ( 14 )

#define ADDR_A15_PIN  		PIN_PC5   // 19
#define ADDR_A16_PIN  		PIN_PC4   // 18
#define ADDR_A17_PIN  		PIN_PD1
#define ADDR_A18_PIN  		PIN_PD0

#define RST_PIN		2


//  Set the EEPROM Addresses
//  |-------------------------------------------------------------------|
//  |	D7    	D6    	D5    	D4    	D3    	D2    	D1    	D0    	|
//  |-------------------------------------------------------------------|
//  |	A18     A17     A16     A15     AO14    AO13    SW1     SW2		|
//  |-------------------------------------------------------------------|
//static const uint8_t Addressing[] PROGMEM = { 0x03, 0x07, 0x0B, 0x0F, 0x13, 0x17, 0x1B, 0x1F, 0x23, 0x27, 0x2B, 0x2F, 0x33, 0x37, 0x3F, 0x47 };

static const uint8_t segNum[11]		PROGMEM = { 0xC0, 0xF9, 0xA4, 0xB0, 0x99, 0x92, 0x82, 0xF8, 0x80, 0x90, 0xBF };



uint8_t digit_1;
uint8_t digit_2;

uint8_t num;

uint8_t countByteBanks;

bool OK_seq;

bool toggle7SegLED;


uint8_t Addressing[63];

///////////////////////
// Helper Function  //
/////////////////////
void setPinHIGH( byte pin )
{
  digitalWrite( pin, HIGH );
}

void setPinLOW( byte pin )
{
  digitalWrite( pin, LOW );
}

void shiftOut( uint8_t dataPin, uint8_t latchPin, uint8_t clockPin, uint8_t val )
{
  uint8_t i;
  
  setPinLOW( LATCH_PIN );
  for ( i = 0; i < 8; ++i )
  {
    setPinLOW( CLOCK_PIN );
    //delayMicros( 50 );
      
    if ( !!( val & ( 1 << (7 - i) ) ) )
    {
      setPinHIGH( SDATA_PIN );
      //delayMicros( 50 );
    }
    else
    {
      setPinLOW( SDATA_PIN );
      //delayMicros( 50 );
    }
    
    setPinHIGH( CLOCK_PIN );
    //delayMicros( 50 );
  }
  setPinHIGH( LATCH_PIN );
  
}


//  Set the EEPROM Addresses
//  |-------------------------------------------------------------------|
//  |	D7    	D6    	D5    	D4    	D3    	D2    	D1    	D0    	|
//  |-------------------------------------------------------------------|
//  |	A18     A17     A16     A15     AO14    AO13    SW1     SW2		|
//  |-------------------------------------------------------------------|
void IO_Address( uint8_t setAddress )
{
  bool extractOctet = false;
  
  for ( uint8_t i=8; i > 0; --i )
  {
    extractOctet = (bool)( ( setAddress >> (i-1) ) & 0x01 );
    
    switch ( i-1 )
    {
      case 0 :
        //setPort( PORTC, SW_ADDR_1_PIN, extractOctet );
        ( extractOctet ) ? setPinHIGH( SW_ADDR_1_PIN ) : setPinLOW( SW_ADDR_1_PIN );
        break;
        
      case 1 :
        //setPort( PORTC, SW_ADDR_2_PIN, extractOctet );
        ( extractOctet ) ? setPinHIGH( SW_ADDR_2_PIN ) : setPinLOW( SW_ADDR_2_PIN );
        break;
      
      case 2 :
        //setPort( PORTE, SW_ADDR_A13_PIN, extractOctet );
        ( extractOctet ) ? setPinHIGH( SW_ADDR_A13_PIN ) : setPinLOW( SW_ADDR_A13_PIN );
        break;
      
      case 3 :
        //setPort( PORTC, SW_ADDR_A14_PIN, extractOctet );
        ( extractOctet ) ? setPinHIGH( SW_ADDR_A14_PIN ) : setPinLOW( SW_ADDR_A14_PIN );
        break;
      
      case 4 :
        //setPort( PORTC, ADDR_A15_PIN, extractOctet );
        ( extractOctet ) ? setPinHIGH( ADDR_A15_PIN ) : setPinLOW( ADDR_A15_PIN );
        break;
      
      case 5 :
        //setPort( PORTC, ADDR_A16_PIN, extractOctet );
        ( extractOctet ) ? setPinHIGH( ADDR_A16_PIN ) : setPinLOW( ADDR_A16_PIN );
        break;
      
      case 6 :
        //setPort( PORTD, ADDR_A17_PIN, extractOctet );
        ( extractOctet ) ? setPinHIGH( ADDR_A17_PIN ) : setPinLOW( ADDR_A17_PIN );
        break;
      
      case 7 :
        //setPort( PORTD, ADDR_A18_PIN, extractOctet );
        ( extractOctet ) ? setPinHIGH( ADDR_A18_PIN ) : setPinLOW( ADDR_A18_PIN );
        break;

      //default :
      //
      //  break;
    }
  }
}

void IO_Init( void )
{
  // define as output
  pinMode( SW_ADDR_1_PIN, OUTPUT );
  pinMode( SW_ADDR_2_PIN, OUTPUT );
  pinMode( SW_ADDR_A13_PIN, OUTPUT );
  pinMode( SW_ADDR_A14_PIN, OUTPUT );
  pinMode( ADDR_A15_PIN, OUTPUT );
  pinMode( ADDR_A16_PIN, OUTPUT );
  pinMode( ADDR_A17_PIN, OUTPUT );
  pinMode( ADDR_A18_PIN, OUTPUT );
  
  pinMode( SEG_EN0_PIN, OUTPUT );
  pinMode( SEG_EN1_PIN, OUTPUT );
  pinMode( SDATA_PIN, OUTPUT );
  pinMode( LATCH_PIN, OUTPUT );
  pinMode( CLOCK_PIN, OUTPUT );
  
  pinMode( RST_PIN, OUTPUT );
  

  // define as input & internal pullup
  pinMode( UP_BUT_PIN, INPUT_PULLUP );
  pinMode( OK_BUT_PIN, INPUT_PULLUP );
  pinMode( DW_BUT_PIN, INPUT_PULLUP );

  // define init 7 segment state
  setPinLOW( SDATA_PIN );
  setPinLOW( LATCH_PIN );
  setPinLOW( CLOCK_PIN );

  // define init reset state
  setPinHIGH( RST_PIN );

}


volatile bool awakeIntTimer = false;

volatile unsigned char  tickCount = 0;
volatile bool     awakeButton = false;

void InitTimer1()
{
  cli();
  
  TCCR1A = 0;
  TCCR1B = 0; 
  
  OCR1A = 0x138;    // 10ms Tick
  
  TCCR1B = ( 1 << WGM12 ) | ( 1 << CS12 );
  TIMSK1 = ( 1 << OCIE1A ); 
  
  sei(); 
  
}

// timer1 overflow interrupt
ISR ( TIMER1_COMPA_vect )
{
  ++tickCount;
  
  if ( tickCount >=20 )    //  120ms
  {
    awakeButton = true;
    tickCount = 0;
  }
  else
    awakeButton = false; 

  awakeIntTimer = true;
  
}

void SevenSegment( uint8_t value, bool dp, bool segEnable_1, bool segEnable_2 )
{
  setPinHIGH( SEG_EN0_PIN );
  setPinHIGH( SEG_EN1_PIN );
  
  // shift out the bits:
  shiftOut( SDATA_PIN, LATCH_PIN, CLOCK_PIN, value &= ( dp ) ? 0x7F : 0xFF );
  
  //delayMillis( 5 );
  
  if ( segEnable_1 )
    setPinLOW( SEG_EN0_PIN );
  else
    setPinHIGH( SEG_EN0_PIN );
  
  if ( segEnable_2 )
    setPinLOW( SEG_EN1_PIN );
  else
    setPinHIGH( SEG_EN1_PIN );
  
}

inline bool getButton_DW( void )
{
	
  return (bool)( digitalRead( DW_BUT_PIN ) );
}

inline bool getButton_UP( void )
{
  
  return (bool)( digitalRead( UP_BUT_PIN ) );
}

inline bool getButton_OK( void )
{
  
  return (bool)( digitalRead( OK_BUT_PIN ) );
}

volatile unsigned char DebounceSwitch( void )
{
  unsigned char Result = 0;
  
  cli();
  if ( awakeButton )
  {
    if ( !getButton_DW() == true )
    {
      Result = 2;
    }
        
    if ( !getButton_UP() == true )
    {
      Result = 4;
    }
    
    if ( !getButton_OK() == true )
    {
      Result = 8;
    }
  }
  else
  {
    Result = 0;
  }
  
  awakeButton = false;
  
  sei();
  
  return Result;
}

// -- SETUP -- //
void setup()
{     
	num = EEPROM.read( 0x02 );
	
	countByteBanks = EEPROM.read( 0x03 );
	
	for ( byte count = 0; count < countByteBanks;  count++ )
	{
		Addressing[count] = EEPROM.read( 0x10 + count );
	}
  
	if ( num < 1 || num > 60 )
		num = 1;
	
	IO_Init();
	//IO_Address( pgm_read_byte( &Addressing[num-1] ) );
	IO_Address( Addressing[num-1] );
      
	awakeIntTimer = false;
	InitTimer1();
    
	uint8_t digit_1 = 1;
	uint8_t digit_2 = 0;
  
	toggle7SegLED = true;
  
	OK_seq = false;
  
	//  Enable global interrupts
	sei();

}

void loop()
{
  if ( awakeIntTimer )
    {		
		//event to be executed every 12ms here
		volatile byte ButtonStatus = DebounceSwitch();
		
		
      //if ( !getButton_OK() )
      if ( ButtonStatus == 8 )
      {
        digit_1 = 10;
        digit_2 = 10;
        OK_seq = true;
      }
      else
      {
        //if ( !getButton_DW() )
        if ( ButtonStatus == 2 )
          num--;
        
        //if ( !getButton_UP() )
        if ( ButtonStatus == 4 )
          num++;
              
        digit_1 = num % 10;
        digit_2 = (uint8_t)( ( num / 10 ) % 10 );
      
        if ( (num < 1) || (num > 20) )
          num = 20;
      }
      
      if ( toggle7SegLED )
        SevenSegment( pgm_read_byte( &segNum[digit_2] ), false, false, true );
      else
        SevenSegment( pgm_read_byte( &segNum[digit_1] ), false, true, false );
                    
      toggle7SegLED ^= 1;
      
      if ( OK_seq )
      {
        delay( 500 );
        
        EEPROM.update( 0x02, num );
        
        //IO_Address( pgm_read_byte( &Addressing[num-1] ) );
		IO_Address( Addressing[num-1] );
        
        delay( 50 );
        
        setPinLOW( RST_PIN );
        delay( 330 );
        setPinHIGH( RST_PIN );
        
        OK_seq = false;
      }
	  
	  ButtonStatus = 0;
	  
	  awakeIntTimer = false;
    }

}
