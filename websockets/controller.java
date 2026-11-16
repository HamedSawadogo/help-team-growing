@Controller
class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/message-prive")
    public void envoyerMessagePrive(MessageChat message) {
        // Envoie le message UNIQUEMENT à l'utilisateur 'destinataire'
        // Spring traduit cela vers la file : /user/{destinataire}/queue/messages
        messagingTemplate.convertAndSendToUser(
                message.getDestinataire(), // ex: "Bob" (doit correspondre au Principal.getName())
                "/queue/messages", // La boîte de réception privée
                message // L'objet converti automatiquement en JSON
        );
    }
}
